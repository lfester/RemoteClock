package com.fester.server;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClockServer implements ClockCommands {

    private Clock clock;

    private String lastResponse = null;

    private final MulticastSocket socket;
    private final InetAddress netGroup;

    public ClockServer() throws IOException {
        socket = new MulticastSocket(81);

        // 239.0.0.0 is the multicast-address for private use within an organization
        // https://en.wikipedia.org/wiki/Multicast_address (Administratively scoped)
        netGroup = InetAddress.getByName("239.0.0.0");

        // Shared clock for all clients
        clock = new Clock();

        // Open the connection
        socket.joinGroup(netGroup);

        // Blocking while the server is running
        serverLoop();

        socket.leaveGroup(netGroup);
        socket.close();
    }

    @Override
    public void start() throws IllegalCmdException {
        clock.start();
        setResponse("Clock started");
    }

    @Override
    public void reset() throws IllegalCmdException {
        clock.reset();
        setResponse("Clock resetted");
    }

    @Override
    public long getTime() throws IllegalCmdException {
        long elapsedTime = clock.getTime();
        setResponse("elapsed time = " + elapsedTime + "ms");
        return elapsedTime;
    }

    @Override
    public void waitTime(long time) throws IllegalCmdException {
        clock.waitTime(time);
        setResponse("Wait finished");
    }

    @Override
    public long halt() throws IllegalCmdException {
        long haltedAtTime = clock.halt();
        setResponse("clock halted, elapsed time = " + haltedAtTime + "ms");
        return haltedAtTime;
    }

    @Override
    public void conTinue() throws IllegalCmdException {
        clock.conTinue();
        setResponse("Clock continued");
    }

    @Override
    public void exit() throws IllegalCmdException {
        clock.exit();
        clock = null;
        setResponse("Programm stop");
    }

    private void setResponse(String msg) {
        lastResponse = msg;
    }

    public String getLastResponse() {
        return lastResponse;
    }

    // =============================
    // Server Handling
    // =============================

    private void serverLoop() {
        System.out.println("Server started!");

        try {
            while (true) {
                DatagramPacket recvPacket = new DatagramPacket(new byte[500], 500);
                DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, netGroup, 2112);

                socket.receive(recvPacket);

                // Receive handling
                String serializedCommand = new String(recvPacket.getData(), 0, recvPacket.getLength(), StandardCharsets.UTF_8);
                System.out.println("Received command: " + serializedCommand);
                Command command = Command.deserialize(serializedCommand);
                if (command == null) {
                    command = new Command(-1);
                }

                // Response handling
                String response = processCommand(command);
                System.out.println("Sending response: " + response);
                sendPacket.setData(response.getBytes(StandardCharsets.UTF_8));
                socket.send(sendPacket);

                // Received exit Command. Stop Server
                if (command.cmd == ClockCommands.CMD_EXIT) {
                    System.out.println("Server stopped!");
                    return;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in server loop: " + ex.getMessage());
        }
    }

    public String processCommand(Command command) {
        if (command == null) {
            command = new Command(-1);
        }

        // Execute the command and get it's response
        String response = null;
        try {
            switch (command.cmd) {
                case ClockCommands.CMD_CONTINUE -> conTinue();
                case ClockCommands.CMD_GETTIME -> getTime();
                case ClockCommands.CMD_START -> start();
                case ClockCommands.CMD_WAIT -> waitTime(command.parameter);
                case ClockCommands.CMD_HALT -> halt();
                case ClockCommands.CMD_RESET -> reset();
                case ClockCommands.CMD_EXIT -> response = "Closing connection..";
                default -> response = "Invalid command received";
            }

            if (response == null)
                response = getLastResponse();
        } catch (IllegalCmdException ex) {
            response = ex.getMessage();
        }

        return response;
    }

    public static void main(String[] args) throws IOException {
        new ClockServer();
    }
}
