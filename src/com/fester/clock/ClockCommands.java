package com.fester.clock;

import com.fester.server.IllegalCmdException;

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


    void start() throws IllegalCmdException;

    void reset() throws IllegalCmdException;

    long getTime() throws IllegalCmdException;

    void waitTime(long time) throws IllegalCmdException;

    long halt() throws IllegalCmdException;

    void conTinue() throws IllegalCmdException;

    void exit() throws IllegalCmdException;
}
