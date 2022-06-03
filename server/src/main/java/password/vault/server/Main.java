package password.vault.server;

import javafx.application.Application;
import password.vault.server.gui.ServerGUI;

public class Main {
    public static void main(String[] args) {
        Server server = ServerConfigurator.getServer();
        if (consoleMode(args)) {
            server.start();
        } else {
            ServerGUI.setServer(server);
            Application.launch(ServerGUI.class);
        }
    }

    private static boolean consoleMode(String[] args) {
        return args.length != 0 && Boolean.parseBoolean(args[0]);
    }
}
