package password.vault.client.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import password.vault.api.Response;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.util.Optional;

public class IndexController {
    private Client client;
    private String username;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblWelcome;

    public IndexController() {
        Context context = Context.getInstance();

        this.client = context.getClient();
        this.username = context.getLoggedInUsername();
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
        Context context = Context.getInstance();

        context.setLoggedInUsername("");
        StageManager stageManager = context.getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
    }

    private void showAlertMessage(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }
}
