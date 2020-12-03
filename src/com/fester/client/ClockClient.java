package com.fester.client;

import com.fester.clock.ClockCommands;
import com.fester.clock.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class ClockClient implements ClockStubListener {
    // displays a message on the screen
    void display(String msg) {
        System.out.println(" " + msg);
    }

    // sends a prompt message for user (command) input
    void prompt(String msg) {
        System.out.print(msg);
    }

    // read syntactically correct command from keyboard
    Command getCommand() {
        // allowed commands as regular expression
        Pattern commandSyntax = Pattern.compile("s|c|h|r|e|g|w +[1-9][0-9]*");
        String cmdText = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // loop until correct command has been entered
        while (true) {
            try {
                prompt("command [s|c|h|r|e|g|w]: ");
                cmdText = in.readLine();
                if (!commandSyntax.matcher(cmdText).matches())
                    throw new Exception();
                break; // leave loop here if correct command detected
            } catch (Exception e) {
                display("command syntax error");
            }
        }

        //   decode command and return with Command object
        StringTokenizer st = new StringTokenizer(cmdText);
        // first token=command
        // second token = parameter
        // case 'e' and any other char
        return switch (st.nextToken().charAt(0)) {
            case 'c' -> new Command(ClockCommands.CMD_CONTINUE);
            case 'g' -> new Command(ClockCommands.CMD_GETTIME);
            case 's' -> new Command(ClockCommands.CMD_START);
            case 'w' -> new Command(ClockCommands.CMD_WAIT, Long.parseLong(st.nextToken()));
            case 'h' -> new Command(ClockCommands.CMD_HALT);
            case 'r' -> new Command(ClockCommands.CMD_RESET);
            default -> new Command(ClockCommands.CMD_EXIT);
        };
    }

    void run() throws IOException {
        ClockStub clockStub = new ClockStub("localhost", 2112);

        // Register to receive server response
        clockStub.registerListener(this);

        display("accepted commands:");
        display("s[tart] h[old] c[ontinue] r[eset])");
        display("g[et time] e[xit] w[ait]\n");

        Command command;
        do {
            // Read command from console
            command = getCommand();
            switch (command.cmd) {
                case ClockCommands.CMD_CONTINUE -> clockStub.conTinue();
                case ClockCommands.CMD_GETTIME -> clockStub.getTime();
                case ClockCommands.CMD_START -> clockStub.start();
                case ClockCommands.CMD_WAIT -> clockStub.waitTime(command.parameter);
                case ClockCommands.CMD_HALT -> clockStub.halt();
                case ClockCommands.CMD_RESET -> clockStub.reset();
                case ClockCommands.CMD_EXIT -> clockStub.exit();
                default -> display("Illegal command");
            }
        } while (command.cmd != ClockCommands.CMD_EXIT);
    }

    @Override
    public void onReceiveResponse(String response) {
        display("");
        display(response);
    }

    public static void main(String[] args) throws IOException {
        (new ClockClient()).run();
    }
}
