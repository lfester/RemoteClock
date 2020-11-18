package com.fester.server;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClockThread extends Thread {

    private final Socket socket;

    public ClockThread(Socket talkSocket) {
        this.socket = talkSocket;
    }

    @Override
    public void run() {
        super.run();

        BufferedReader fromClient = null;
        OutputStreamWriter toClient = null;

        try {
            fromClient = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), "Cp850"));

            toClient = new OutputStreamWriter(
                    socket.getOutputStream(), "Cp850");


            ClockServer server = new ClockServer();

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
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();

                if (fromClient != null)
                    fromClient.close();

                if (toClient != null)
                    toClient.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
