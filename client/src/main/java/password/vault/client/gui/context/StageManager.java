package password.vault.client.gui.context;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import password.vault.client.gui.model.FXMLScenes;

import java.io.IOException;
import java.net.URL;

public class StageManager {
    private final Stage primaryStage;

    public StageManager(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setResizable(false);
        primaryStage.setTitle("Password Vault");

        primaryStage.show();
    }

    public void switchScene(FXMLScenes applicationFXMLScene) {
        try {
            Parent root = getParentNode(applicationFXMLScene);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("unable to change scenes, ", e);
        }
    }

    public Object getSceneController(FXMLScenes applicationFXMLScene) {
        FXMLLoader rootSceneLoader = new FXMLLoader(applicationFXMLScene.getFileURL());
        return rootSceneLoader.getController();
    }

    public void displayStartingScene() {
        switchScene(FXMLScenes.LOGIN);
    }

    private Parent getParentNode(FXMLScenes applicationFXMLScene) throws IOException {
        URL rootSceneURL = getClass().getResource(applicationFXMLScene.getFxmlFilename());
        FXMLLoader rootSceneLoader = new FXMLLoader(rootSceneURL);
        return rootSceneLoader.load();
    }

    public Window getCurrentStage() {
        return primaryStage;
    }
}
