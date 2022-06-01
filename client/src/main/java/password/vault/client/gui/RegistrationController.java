package password.vault.client.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import password.vault.client.Client;

public class RegistrationController {
    private Client client;

    private Stage primaryStage;

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
    void btnExitClicked(ActionEvent event) {

    }

    @FXML
    void btnRegisterClicked(ActionEvent event) {

    }

}
