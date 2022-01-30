package password.vault.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ServerCommand {
    REGISTER("register", 4, "register <user> <email> <password> <password-repeat>"),
    LOGIN("login", 2, "login <user> <password>"),
    LOGOUT("logout", 0, "logout"),
    DISCONNECT("disconnect", 0, "disconnect"),
    ADD_PASSWORD("add-password", 3, "add-password <website> <user> <password>"),
    REMOVE_PASSWORD("remove-password", 2, "remove-password <website> <user>"),
    // UPDATE_PASSWORD("update-password", 4, "update-password <website> <user> <oldpass> <newpass>"),
    RETRIEVE_CREDENTIALS("get-password", 2, "get-password <website> <user>"),
    GENERATE_PASSWORD("generate-password", 3, "generate-password <website> <user> <passwordLength>"),
    HELP("help", 0, ""),
    UNKNOWN("", 0, "");

    private final String commandText;
    private final int numberOfArguments;
    private final String commandOverview;
    private static final Map<String, ServerCommand> COMMAND_TEXT_TO_ENUM_VALUE;

    ServerCommand(String commandText, int numberOfArguments, String commandOverview) {
        this.commandText = commandText;
        this.numberOfArguments = numberOfArguments;
        this.commandOverview = commandOverview;
    }

    static {
        Map<String, ServerCommand> map = new HashMap<>();

        for (ServerCommand instance : ServerCommand.values()) {
            map.put(instance.getCommandText().toLowerCase(), instance);
        }

        COMMAND_TEXT_TO_ENUM_VALUE = Collections.unmodifiableMap(map);
    }

    public String getCommandText() {
        return commandText;
    }

    public int getNumberOfArguments() {
        return numberOfArguments;
    }

    public String getCommandOverview() {
        return commandOverview;
    }

    public static String printHelp() {
        StringBuilder sb = new StringBuilder();
        for (ServerCommand instance : ServerCommand.values()) {
            sb.append(instance.commandOverview);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static ServerCommand getServerCommandFromCommandText(String commandText) {
        return COMMAND_TEXT_TO_ENUM_VALUE.getOrDefault(commandText.toLowerCase(Locale.ROOT), UNKNOWN);
    }
}

