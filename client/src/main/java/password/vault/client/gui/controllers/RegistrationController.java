package password.vault.client.gui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.context.Context;
import password.vault.client.gui.model.FXMLScenes;
import password.vault.client.gui.context.StageManager;
import password.vault.client.gui.model.RegistrationRequest;

import java.io.IOException;
import java.util.Optional;

public class RegistrationController {

    private final Client client;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtPasswordShown;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtPasswordRepeted;

    @FXML
    private TextField txtMasterPasswordShown;

    @FXML
    private PasswordField txtMasterPassword;

    @FXML
    private PasswordField txtMasterPasswordRepeated;

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
    void initialize() {
        setupDefaultButton();
        CommonUIElements.setupShowHidePasswordCheckbox(txtPasswordShown, txtPassword, chBoxShowPassword);
        CommonUIElements.setupShowHidePasswordCheckbox(txtMasterPasswordShown, txtMasterPassword, chBoxShowMasterPassword);
    }

    @FXML
    void btnBackToLoginClicked(ActionEvent event) {
        StageManager stageManager = Context.getInstance().getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
    }

    @FXML
    void btnExitClicked(ActionEvent event) {
        Optional<ButtonType> result = CommonUIElements.getQuitAlert().showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }
            try {
                client.sendRequest(ServerTextCommandsFactory.disconnectCommand());
                Response response = client.receiveResponse();
                client.closeConnection();

                // this may be removed
                CommonUIElements.getInformationAlert(response.message(), "").showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                CommonUIElements.getFailedRequestWarningAlert().showAndWait();
            }

            Platform.exit();
            System.exit(0);
        });
    }

    @FXML
    void btnRegisterClicked(ActionEvent event) {
        try {
            RegistrationRequest registrationRequest = new RegistrationRequest(txtUsername.getText(),
                                                                              txtEmail.getText(),
                                                                              txtPassword.getText(),
                                                                              txtPasswordRepeted.getText(),
                                                                              txtMasterPassword.getText(),
                                                                              txtMasterPasswordRepeated.getText());

            client.sendRequest(ServerTextCommandsFactory.registerCommand(registrationRequest.username(),
                                                                         registrationRequest.email(),
                                                                         registrationRequest.password(),
                                                                         registrationRequest.passwordRepeated(),
                                                                         registrationRequest.masterPassword(),
                                                                         registrationRequest.masterPasswordRepeated()));
            Response response = client.receiveResponse();
            if (response.serverResponse().equals(ServerResponses.REGISTRATION_SUCCESS)) {
                CommonUIElements.getInformationAlert(response.message(), "registration success").showAndWait();

                switchToLoginScene();
            } else {
                CommonUIElements.getErrorAlert(response.message()).showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert().showAndWait();
        } catch (RegistrationRequest.RegistrationRequestException e) {
            CommonUIElements.getErrorAlert(e.getMessage()).showAndWait();
        }
    }

    private void setupDefaultButton() {
        btnRegister.setDefaultButton(true);

        btnRegister.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                btnRegister.fire();
            }
        });
    }

    private void switchToLoginScene() {
        Context context = Context.getInstance();

        StageManager stageManager = context.getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
    }
}

