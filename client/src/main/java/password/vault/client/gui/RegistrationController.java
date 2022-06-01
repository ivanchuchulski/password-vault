package password.vault.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import password.vault.api.Response;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.util.Optional;

public class RegistrationController {

    private Client client;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtPasswordRepeted;

    @FXML
    private PasswordField txtMasterPassword;

    @FXML
    private PasswordField txttMasterPasswordRepeated;

    @FXML
    private Button btnRegister;

    @FXML
    private CheckBox chBoxShowMasterPassword;

    @FXML
    private CheckBox chBoxShowPassword;

    @FXML
    private Button btnExit;

    @FXML
    private Button btnBackToLogin;

    public RegistrationController() {
        this.client = Context.getInstance().getClient();
    }

    @FXML
    void btnBackToLoginClicked(ActionEvent event) {
        StageManager stageManager = Context.getInstance().getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
    }

    @FXML
    void btnExitClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Quit confirmation");
        alert.setHeaderText("Quitting Password Vault");
        alert.setContentText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {

                client.sendRequest(ServerTextCommandsFactory.disconnectCommand());
                try {
                    Response response = client.receiveResponse();
                    client.closeConnection();
                    showAlertMessage(Alert.AlertType.INFORMATION, response.message(), "");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Platform.exit();
                System.exit(0);
            }
        });
    }

    @FXML
    void btnRegisterClicked(ActionEvent event) {

    }

    private void showAlertMessage(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }
}

