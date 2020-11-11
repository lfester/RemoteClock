package com.fester.client;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClockStub implements ClockCommands {

    private String lastResponse = null;

    private BufferedReader fromServer = null;
    private OutputStreamWriter toServer = null;

    public ClockStub() {
        try {
            Socket talkSocket = new Socket("localhost", 4711);
            fromServer = new BufferedReader(
                    new InputStreamReader(
                            talkSocket.getInputStream(), "Cp850"));
            toServer =
                    new OutputStreamWriter(
                            talkSocket.getOutputStream(), "Cp850");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readServerResponse() {
        try {
            lastResponse = fromServer.readLine();
        } catch (Exception ex) {
            lastResponse = "Invalid server response\n";
        }
    }

    private void sendCommand(Command command) {
        try {
            toServer.write(command.serialize() + "\n");
            toServer.flush();

            readServerResponse();
        } catch (Exception ex) {
            lastResponse = "[ClientStub] Error while writing command";
        }
    }

    @Override
    public void start() {
        sendCommand(new Command(ClockCommands.CMD_START));
    }

    @Override
    public void reset() {
        sendCommand(new Command(ClockCommands.CMD_RESET));
    }

    @Override
    public long getTime() {
        sendCommand(new Command(ClockCommands.CMD_GETTIME));
        return 0;
    }

    @Override
    public void waitTime(long time) {
        sendCommand(new Command(ClockCommands.CMD_WAIT, time));
    }

    @Override
    public long halt() {
        sendCommand(new Command(ClockCommands.CMD_HALT));
        return 0;
    }

    @Override
    public void conTinue() {
        sendCommand(new Command(ClockCommands.CMD_CONTINUE));
    }

    @Override
    public void exit() {
        sendCommand(new Command(ClockCommands.CMD_EXIT));
    }

    public String getLastResponse() {
        return lastResponse;
    }
}
