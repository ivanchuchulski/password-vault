package password.vault.client.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import password.vault.api.Response;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class IndexController {
    public static final String ROOT_SCENE_FXML_FILENAME = "index.fxml";
    private Client client;
    private String username;

    private Stage primaryStage;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblWelcome;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    void btnLogoutClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit confirmation");
        alert.setHeaderText("Quitting Password Vault");
        alert.setContentText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    client.sendRequest(ServerTextCommandsFactory.logoutCommand());
                    Response response = client.receiveResponse();
                    showAlertMessage(Alert.AlertType.INFORMATION, response.message(), "");

                    switchToLoginScene();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    private void switchToLoginScene() {
        try {
            URL rootSceneURL = getClass().getResource(LoginController.ROOT_SCENE_FXML_FILENAME);

            if (rootSceneURL == null) {
                throw new RuntimeException("could not load index scene fxml");
            }

            FXMLLoader rootSceneLoader = new FXMLLoader(rootSceneURL);
            Parent root = rootSceneLoader.load();

            LoginController loginController = rootSceneLoader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.setClient(client);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlertMessage(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }
}
