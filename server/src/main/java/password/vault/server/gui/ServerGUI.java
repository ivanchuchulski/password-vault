package password.vault.server.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import password.vault.server.Server;

import java.io.IOException;
import java.net.URL;

public class ServerGUI extends Application {
    private static Server server;

    public static void setServer(Server server) {
        ServerGUI.server = server;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            URL rootSceneURL = getClass().getResource(CommandPanel.SCENE_FXML_FILENAME);
            FXMLLoader rootSceneLoader = new FXMLLoader(rootSceneURL);
            Parent root = rootSceneLoader.load();

            CommandPanel controller = rootSceneLoader.getController();
            controller.setServer(server);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Password Vault Server Control Panel");

            primaryStage.setOnCloseRequest(event -> {
                server.stop();
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("unable to start main scene", e);
        }
    }
}
