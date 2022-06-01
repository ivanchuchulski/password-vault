package password.vault.client;

import javafx.application.Application;
import password.vault.client.console.ConsoleClient;
import password.vault.client.gui.GUIClient;

public class Main {
    public static void main(String[] args) {
        Client client = new Client();

        if (consoleMode(args)) {
            ConsoleClient consoleClient = new ConsoleClient(client);
            consoleClient.run();
        } else {
            GUIClient.setClient(client);
            Application.launch(GUIClient.class);
        }
    }

    private static boolean consoleMode(String[] args) {
        return args.length != 0 && Boolean.parseBoolean(args[0]);
    }
}
