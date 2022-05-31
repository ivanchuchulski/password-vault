package password.vault.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import password.vault.api.Response;
import password.vault.api.ServerCommand;
import password.vault.client.Client;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    private Client client;

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

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    void btnLoginClicked(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.length() == 0 || password.length() == 0) {
            showAlertMessage(Alert.AlertType.WARNING, "Fields are necessary!", "");
            return;
        }

        client.sendRequest(loginCommand(username, password));
        try {
            Response response = client.receiveResponse();
            showAlertMessage(Alert.AlertType.INFORMATION, response.message(), "");
        } catch (IOException e) {
            e.printStackTrace();
            showAlertMessage(Alert.AlertType.WARNING, "Couldn't complete your request!", "");
        }
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
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void showAlertMessage(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }

    private String loginCommand(String username, String password) {
        return String.format("%s %s %s", ServerCommand.LOGIN.getCommandText(), username, password);
    }

}

