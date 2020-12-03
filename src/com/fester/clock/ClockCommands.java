package com.fester.clock;

import com.fester.server.IllegalCmdException;

import java.io.IOException;

public interface ClockCommands {
    int CMD_START = 1;
    int CMD_STOP = 2;
    int CMD_EXIT = 3;
    int CMD_HALT = 4;
    int CMD_WAIT = 5;
    int CMD_CONTINUE = 6;
    int CMD_GETTIME = 7;
    int CMD_RESET = 8;
    int CMD_NOT_EXECUTED = 9;


    void start() throws IllegalCmdException, IOException;

    void reset() throws IllegalCmdException, IOException;

    long getTime() throws IllegalCmdException, IOException;

    void waitTime(long time) throws IllegalCmdException, IOException;

    long halt() throws IllegalCmdException, IOException;

    void conTinue() throws IllegalCmdException, IOException;

    void exit() throws IllegalCmdException, IOException;
}
