package com.fester.clock;

public class Command {
    public int cmd; // stored command
    public long parameter; // stored parameter

    // create Command without parameter
    public Command(int cmd) {
        this.cmd = cmd;
    }

    // create Command with parameter
    public Command(int cmd, long parameter) {
        this.cmd = cmd;
        this.parameter = parameter;
    }

    public String serialize() {
        String result = "c=" + this.cmd;

        if (parameter > 0) {
            result += ";p=" + parameter;
        }
        return result;
    }

    public static Command deserialize(String string) {
        try {
            String[] elements = string.split(";");

            int command = -1;
            long parameter = 0;

            for (String message : elements) {
                String[] messageParts = message.split("=");

                if (messageParts.length != 2)
                    throw new IllegalArgumentException("Malformed parameter: \"" + parameter + "\n");

                switch (messageParts[0]) {
                    case "c" -> command = Integer.parseInt(messageParts[1]);
                    case "p" -> parameter = Long.parseLong(messageParts[1]);
                }
            }

            if (command != -1) {
                return new Command(command, parameter);
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
