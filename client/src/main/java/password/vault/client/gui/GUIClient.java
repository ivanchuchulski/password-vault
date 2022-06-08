package password.vault.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.context.Context;
import password.vault.client.gui.context.StageManager;

import java.io.IOException;

public class GUIClient extends Application {
    private static Client client;

    public static void setClient(Client client) {
        GUIClient.client = client;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            try {
                client.sendRequest(ServerTextCommandsFactory.disconnectCommand());
                client.closeConnection();
            } catch (IOException e) {
                Platform.exit();
                System.exit(0);
                throw new RuntimeException("unable to close connection", e);
            }

            Platform.exit();
            System.exit(0);
        });

        StageManager stageManager = new StageManager(primaryStage);
        Context context = Context.getInstance();

        context.setClient(client);
        context.setStageManager(stageManager);
        context.setLoggedInUsername("");

        stageManager.displayStartingScene();
    }
}
