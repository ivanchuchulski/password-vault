package password.vault.client.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import password.vault.api.Response;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.Context;
import password.vault.client.gui.FXMLScenes;
import password.vault.client.gui.StageManager;

import java.io.IOException;
import java.util.Optional;

public class IndexController {
    private final Client client;
    private final String username;

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
    void initialize() {
        lblWelcome.setText(lblWelcome.getText() + username + "!");

        try {
            client.sendRequest(ServerTextCommandsFactory.getAllCredentials());
            Response response = client.receiveResponse();
            System.out.println(response.message());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btnLogoutClicked(ActionEvent event) {
        Alert alert = CommonUIElements.getQuitAlert();
        Optional<ButtonType> result = alert.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    client.sendRequest(ServerTextCommandsFactory.logoutCommand());

                    Response response = client.receiveResponse();

                    CommonUIElements.getInformationAlert(response.message(), "").showAndWait();
                    switchToLoginScene();
                } catch (IOException e) {
                    e.printStackTrace();
                    CommonUIElements.getFailedRequestWarningAlert();
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
}
