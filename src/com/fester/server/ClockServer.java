package com.fester.server;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ClockServer {

    Selector events;
    ServerSocketChannel listenChannel;

    public ClockServer(int port) throws IOException {
        events = Selector.open();

        // create a non-blocking server socket and
        // register it for connection request events
        listenChannel = ServerSocketChannel.open();
        listenChannel.configureBlocking(false);
        listenChannel.socket().bind(new InetSocketAddress(port));
        listenChannel.register(events, SelectionKey.OP_ACCEPT);
    }

    // process OP_READ event
    void processRead(SelectionKey selKey) {
        // process OP_READ event
        SocketChannel talkChan = null;
        try {
            // get the channel with the read event
            talkChan = (SocketChannel) selKey.channel();

            ServerClock clock = (ServerClock) selKey.attachment();

            String serializedCommand = ChannelRW.recvTextMessage(talkChan);
            System.out.println("Received: " + serializedCommand);

            Command command = Command.deserialize(serializedCommand);
            if (command == null) {
                command = new Command(-1);
            }

            // Execute the command and get it's response
            String response = null;
            try {
                switch (command.cmd) {
                    case ClockCommands.CMD_CONTINUE -> clock.conTinue();
                    case ClockCommands.CMD_GETTIME -> clock.getTime();
                    case ClockCommands.CMD_START -> clock.start();
                    case ClockCommands.CMD_WAIT -> clock.waitTime(command.parameter);
                    case ClockCommands.CMD_HALT -> clock.halt();
                    case ClockCommands.CMD_RESET -> clock.reset();
                    case ClockCommands.CMD_EXIT -> response = "Closing connection..";
                    default -> response = "Invalid command received";
                }

                if (response == null)
                    response = clock.getLastResponse();

            } catch (IllegalCmdException ex) {
                response = ex.getMessage();
            }

            ChannelRW.sendTextMessage(talkChan, response);

            if (command.cmd == ClockCommands.CMD_EXIT)
                talkChan.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            try { // always try to close talkChannel
                talkChan.close();
            } catch (IOException ignore) {
            }
        }
    }

    // process OP_ACCEPT event
    void processAccept() {
        // process OP_ACCEPT event
        SocketChannel talkChannel = null;
        try {
            // The returned talkChannel is in blocking mode.
            talkChannel = listenChannel.accept();
            talkChannel.configureBlocking(false);
            SelectionKey key = talkChannel.register(events, SelectionKey.OP_READ);
            key.attach(new ServerClock());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            try { // always try to close talkChannel
                talkChannel.close();
            } catch (IOException ignore) {
            }
        }
    }

    // infinite server loop
    public void serverLoop() throws IOException {
        Iterator<SelectionKey> selKeys;
        // infinite server loop
        while (true) {
            // blocks until event occurs
            events.select();

            // process all pending events (might be more than 1)
            selKeys = events.selectedKeys().iterator();

            while (selKeys.hasNext()) {
                // get the selection key for the next event ...
                SelectionKey selKey = selKeys.next();

                // ... and remove it from the list to indicate
                // that it is being processed
                selKeys.remove();

                // [ process single event .. ]
                if (selKey.isReadable()) {
                    // it is a "data are available to be read" event
                    processRead(selKey);
                } else if (selKey.isAcceptable()) {
                    // it is a "remote socket wants to connect" event
                    processAccept();

                    System.out.println("Connected");
                } else {
                    System.out.println("Unknown event occured");
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            (new ClockServer(4711)).serverLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
