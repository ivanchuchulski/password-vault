package password.vault.server.communication;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserRequest {
    private final SocketChannel socketChannel;
    private final String command;
    private final String[] arguments;

    public UserRequest(SocketChannel socketChannel, String clientInput) {
        List<String> tokens = getCommandArguments(clientInput);
        this.socketChannel = socketChannel;
        command = tokens.get(0);
        arguments = tokens.subList(1, tokens.size()).toArray(new String[0]);
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public String command() {
        return command;
    }

    public String[] arguments() {
        return arguments;
    }

    private static List<String> getCommandArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            }
            if (c == ' ' && !insideQuote) { //when space is not inside quote split
                tokens.add(sb.toString().replace("\"", "")); //token is ready, lets add it to list
                sb.delete(0, sb.length()); //and reset StringBuilder`s content
            } else {
                sb.append(c);//else add character to token
            }
        }
        //let's not forget about last token that doesn't have space after it
        tokens.add(sb.toString().replace("\"", ""));

        return tokens;
    }

    public int numberOfArguments() {
        return arguments.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRequest that = (UserRequest) o;
        return Objects.equals(command, that.command) && Arrays.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(command);
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }

    @Override
    public String toString() {
        return "UserCommand{" +
                "command='" + command + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
