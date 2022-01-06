package password.vault.server.communication;

public record UserCommand(String command, String[] arguments) {

    public int numberOfArguments() {
        return arguments.length;
    }
}
