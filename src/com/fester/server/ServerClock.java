package com.fester.server;

import com.fester.clock.ClockCommands;

public class ServerClock implements ClockCommands {

    private Clock clock;
    private String lastResponse;

    public ServerClock() {
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
}
