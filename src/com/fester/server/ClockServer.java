package com.fester.server;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClockServer implements ClockCommands {

    private Clock clock;

    private String lastResponse = null;

    public ClockServer() {
        clock = new Clock();
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


    public static void main(String[] args) {
        Socket talkSocket;
        BufferedReader fromClient;
        OutputStreamWriter toClient;

        ClockServer server = new ClockServer();

        boolean running = true;

        try {
            ServerSocket listenSocket = new ServerSocket(4711);

            while (running) {
                talkSocket = listenSocket.accept();

                fromClient = new BufferedReader(new InputStreamReader(
                        talkSocket.getInputStream(), "Cp850"));

                // outgoing messages are char based (text)
                toClient = new OutputStreamWriter(
                        talkSocket.getOutputStream(), "Cp850");

                commandLoop:
                while (true) {
                    String serializedCommand = fromClient.readLine();
                    System.out.println("Received: " + serializedCommand);

                    Command command = Command.deserialize(serializedCommand);

                    // Received an invalid command. Set it to '-1' to get handled by
                    // the default switch-statement
                    if (command == null) {
                        command = new Command(-1);
                    }

                    // Execute the command and get it's response
                    String response = null;
                    try {
                        switch (command.cmd) {
                            case ClockCommands.CMD_CONTINUE -> server.conTinue();
                            case ClockCommands.CMD_GETTIME -> server.getTime();
                            case ClockCommands.CMD_START -> server.start();
                            case ClockCommands.CMD_WAIT -> server.waitTime(command.parameter);
                            case ClockCommands.CMD_HALT -> server.halt();
                            case ClockCommands.CMD_RESET -> server.reset();
                            case ClockCommands.CMD_EXIT -> {
                                break commandLoop;
                            }
                            default -> response = "Invalid command received";
                        }

                        if (response == null)
                            response = server.getLastResponse();
                    } catch (IllegalCmdException ex) {
                        response = ex.getMessage();
                    }

                    // Forward the response to the client
                    toClient.write(response + "\n");
                    toClient.flush();
                }

                // We are done with this client. Close the connection
                toClient.close();
                fromClient.close();
                talkSocket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
