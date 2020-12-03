package com.fester.client;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

public class ClockStub implements ClockCommands {

    private MulticastSocket socket;
    private String host;
    private InetAddress netGroup;

    private ClockStubListener listener;

    private Command lastCommand;

    public ClockStub(String host, int port) throws IOException {
        this.host = host;

        netGroup = InetAddress.getByName("239.0.0.0");

        socket = new MulticastSocket(port);

        new Thread(() -> {
            try {
                receiveMessage(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        socket.joinGroup(netGroup);
    }

    private void sendCommand(Command command) throws IOException {
        lastCommand = command;

        DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(host), 81);

        // Send message to server
        sendPacket.setData(command.serialize().getBytes(StandardCharsets.UTF_8));
        socket.send(sendPacket);
    }

    @Override
    public void start() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_START));
    }

    @Override
    public void reset() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_RESET));
    }

    @Override
    public long getTime() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_GETTIME));
        return 0;
    }

    @Override
    public void waitTime(long time) throws IOException {
        sendCommand(new Command(ClockCommands.CMD_WAIT, time));
    }

    @Override
    public long halt() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_HALT));
        return 0;
    }

    @Override
    public void conTinue() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_CONTINUE));
    }

    @Override
    public void exit() throws IOException {
        sendCommand(new Command(ClockCommands.CMD_EXIT));
    }

    private void receiveMessage(MulticastSocket socket) throws IOException {
        DatagramPacket recvPacket = new DatagramPacket(new byte[500], 500);

        do {
            socket.receive(recvPacket);
            String response = new String(recvPacket.getData(), 0, recvPacket.getLength(), StandardCharsets.UTF_8);

            if (listener != null) {
                listener.onReceiveResponse(response);
            }
        } while (lastCommand.cmd != ClockCommands.CMD_EXIT);
    }

    public void registerListener(ClockStubListener listener) {
        this.listener = listener;
    }
}
