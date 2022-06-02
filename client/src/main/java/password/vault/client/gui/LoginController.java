package password.vault.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    private final Client client;

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab tabLogin;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnExit;

    @FXML
    private Hyperlink hypRegistration;

    public LoginController() {
        System.out.println("constructor first");
        this.client = Context.getInstance().getClient();
    }

    @FXML
    void initialize() {
        System.out.println("init method second");

        // make pressing enter fire the login button
        btnLogin.setDefaultButton(true);

        btnExit.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                btnLogin.fire();
            }
        });
    }

    @FXML
    void btnLoginClicked(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.length() == 0 || password.length() == 0) {
            CommonUIElements.getErrorAlert("Fields are necessary!").showAndWait();
            return;
        }

        try {
            client.sendRequest(ServerTextCommandsFactory.loginCommand(username, password));
            Response response = client.receiveResponse();
            ServerResponses serverResponses = response.serverResponse();

            System.out.println(response);

            if (serverResponses.equals(ServerResponses.LOGIN_SUCCESS)) {
                CommonUIElements.getInformationAlert(response.message(), "login success").showAndWait();
                switchToIndexScene(username);
            } else {
                CommonUIElements.getErrorAlert(response.message()).showAndWait();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert();
        }
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

                // this could be removed
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
    void hypRegistrationPressed(ActionEvent event) {
        StageManager stageManager = Context.getInstance().getStageManager();
        stageManager.switchScene(FXMLScenes.REGISTRATION);
    }


    private void switchToIndexScene(String username) {
        Context context = Context.getInstance();

        context.setLoggedInUsername(username);

        StageManager stageManager = context.getStageManager();
        stageManager.switchScene(FXMLScenes.INDEX);
    }
}
