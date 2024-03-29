package password.vault.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum ServerCommand {
    REGISTER("register", 6, "register <user> <email> <password> <password-repeat> <master-password> " +
            "<master-password-repeated>"),
    LOGIN("login", 2, "login <user> <password>"),
    LOGOUT("logout", 0, "logout"),
    DISCONNECT("disconnect", 0, "disconnect"),

    ADD_PASSWORD("add-password", 4, "add-password <website> <user> <password> <master-password>"),
    ADD_PASSWORD_WITH_CHECK("add-password-with-check", 4, "add-password-with-check <website> <user> <password> " +
            "<master-password>"),
    REMOVE_PASSWORD("remove-password", 3, "remove-password <website> <user> <master-password>"),
    RETRIEVE_CREDENTIAL("get-password", 3, "get-password <website> <user> <master-password>"),
    GET_ALL_CREDENTIALS("get-all-credentials", 0, "get-all-credentials"),

    GET_ALL_CREDENTIALS_JSON("get-all-credentials-json", 0, "get-all-credentials-json"),

    GENERATE_PASSWORD("generate-password", 4, "generate-password <website> <user> <passwordLength> <master-password>"),
    CHECK_PASSWORD_SAFETY("check-password", 1, "check-password <password>"),

    HELP("help", 0, "help"),
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
        sb.append("commands overview : ");
        sb.append(System.lineSeparator());

        ServerCommand[] values = ServerCommand.values();

        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].getCommandOverview());
            if (i != values.length - 1) {
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    public static ServerCommand getServerCommandFromCommandText(String commandText) {
        return COMMAND_TEXT_TO_ENUM_VALUE.getOrDefault(commandText.toLowerCase(Locale.ROOT), UNKNOWN);
    }
}

