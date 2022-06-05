package password.vault.client.gui.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import password.vault.api.CredentialIdentifierDTO;
import password.vault.api.Response;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.Context;
import password.vault.client.gui.FXMLScenes;
import password.vault.client.gui.StageManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class IndexController {
    private final Client client;

    private final Gson gson;
    private final String username;

    @FXML
    private Button btnLogout;

    @FXML
    private Label lblWelcome;

    public IndexController() {
        Context context = Context.getInstance();
        this.client = context.getClient();
        this.username = context.getLoggedInUsername();

        this.gson = new Gson();
    }

    @FXML
    void initialize() {
        lblWelcome.setText(lblWelcome.getText() + username + "!");

        try {
            client.sendRequest(ServerTextCommandsFactory.getAllCredentialsJSON());
            Response response = client.receiveResponse();
            System.out.println(response.message());

            Type listType = new TypeToken<List<CredentialIdentifierDTO>>() {}.getType();
            List<CredentialIdentifierDTO> credentials = gson.fromJson(response.message(), listType);

            System.out.println(credentials);
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getErrorAlert("error fetching data from server");
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
