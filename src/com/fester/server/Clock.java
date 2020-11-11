package com.fester.server;

import com.fester.clock.ClockCommands;

public class Clock implements ClockCommands {
    // clock state keycodes
    final static int ST_READY = 0;
    final static int ST_RUNNING = 1;
    final static int ST_HALTED = 2;
    final static int ST_EXIT = 3;

    // internal state variables
    int state; // one of {ST_READY, ST_HALT ... }
    long startTime; // stores start time
    long elapsedTime; // stores elapsed time when halt command occurs

    public Clock() {
        state = ST_READY; // initial state is ST_READY
        startTime = 0;
        elapsedTime = 0;
    }

    public void start() throws IllegalCmdException {
        if (state != ST_READY)
            throw new IllegalCmdException("'start' not allowed in the current context");

        startTime = System.currentTimeMillis();
        state = ST_RUNNING;
    }

    @Override
    public void reset() throws IllegalCmdException {
        if ((state != ST_HALTED) && (state != ST_RUNNING))
            throw new IllegalCmdException("'reset' not allowed in the current context");

        startTime = 0;
        elapsedTime = 0;
        state = ST_READY;
    }

    @Override
    public long getTime() throws IllegalCmdException {
        if ((state != ST_HALTED) && (state != ST_RUNNING))
            throw new IllegalCmdException("'gettime' not allowed in the current context");

        if (state == ST_RUNNING) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        return elapsedTime;
    }

    @Override
    public void waitTime(long time) throws IllegalCmdException {
        if (state != ST_RUNNING)
            throw new IllegalCmdException("'wait' not allowed in the current context");

        try {
            Thread.sleep(time);
        } catch (Exception ignore) {
        }
    }

    @Override
    public long halt() throws IllegalCmdException {
        if (state != ST_RUNNING)
            throw new IllegalCmdException("'halt' not allowed in the current context");

        // freeze the elapsed time
        elapsedTime = System.currentTimeMillis() - startTime;
        state = ST_HALTED;
        return elapsedTime;
    }

    @Override
    public void conTinue() throws IllegalCmdException {
        if (state != ST_HALTED)
            throw new IllegalCmdException("'continue' not allowed in the current context");

        startTime = System.currentTimeMillis() - elapsedTime;
        state = ST_RUNNING;
    }

    @Override
    public void exit() throws IllegalCmdException {
        if (state != ST_READY)
            throw new IllegalCmdException("'exit' not allowed in the current context");

        state = ST_EXIT; // no way out
    }
}
