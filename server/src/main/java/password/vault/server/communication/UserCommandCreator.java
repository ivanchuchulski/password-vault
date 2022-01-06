package password.vault.server.communication;

import java.util.ArrayList;
import java.util.List;

// credit : mjt lab 10
public class UserCommandCreator {
    public static UserCommand createCommand(String clientInput) {
        List<String> tokens = UserCommandCreator.getCommandArguments(clientInput);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        return new UserCommand(tokens.get(0), args);
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
        //lets not forget about last token that doesn't have space after it
        tokens.add(sb.toString().replace("\"", ""));

        return tokens;
    }
}
