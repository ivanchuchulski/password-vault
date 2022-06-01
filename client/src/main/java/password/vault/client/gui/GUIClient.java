package password.vault.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.net.URL;

public class GUIClient extends Application {

    private static final String ROOT_SCENE_FXML_FILENAME = "login.fxml";
    private static Client client;

    public static void setClient(Client client) {
        GUIClient.client = client;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL rootSceneURL = getClass().getResource(ROOT_SCENE_FXML_FILENAME);

        if (rootSceneURL == null) {
            throw new RuntimeException("could not load main scene fxml");
        }

        FXMLLoader rootSceneLoader = new FXMLLoader();
        rootSceneLoader.setLocation(rootSceneURL);

        Parent root = rootSceneLoader.load();

        // inject the Client to the controller
        LoginController rootSceneLoaderController = rootSceneLoader.getController();
        rootSceneLoaderController.setClient(client);

        Scene scene = new Scene(root);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Password Vault");
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
        primaryStage.show();
    }
}
