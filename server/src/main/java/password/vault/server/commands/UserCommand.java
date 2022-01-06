package password.vault.server.commands;

public record UserCommand(String command, String[] arguments) {

    public int numberOfArguments() {
        return arguments.length;
    }
}
