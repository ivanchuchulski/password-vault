package password.vault.client.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.Client;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class LoginController {

    public static final String ROOT_SCENE_FXML_FILENAME = "login.fxml";

    private Client client;
    private Stage primaryStage;

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

    public void setClient(Client client) {
        this.client = client;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public LoginController() {
        System.out.println("constructor first");
        // this.client = Context.getInstance().getClient();
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
            showAlertMessage(Alert.AlertType.WARNING, "Fields are necessary!", "");
            return;
        }

        try {
            client.sendRequest(ServerTextCommandsFactory.loginCommand(username, password));
            Response response = client.receiveResponse();
            ServerResponses serverResponses = response.serverResponse();

            System.out.println(response);

            if (serverResponses.equals(ServerResponses.LOGIN_SUCCESS)) {
                showAlertMessage(Alert.AlertType.INFORMATION, response.message(), "login success");
                switchToIndexScene(username);
            } else {
                showAlertMessage(Alert.AlertType.ERROR, response.message(), "");
            }
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
    void hypRegistrationPressed(ActionEvent event) {

    }

    private void showAlertMessage(Alert.AlertType type, String header, String context) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(context);

        alert.showAndWait();
    }


    private void switchToIndexScene(String username) throws IOException {
        URL rootSceneURL = getClass().getResource(FXMLScenes.INDEX.getFxmlFilename());

        if (rootSceneURL == null) {
            throw new RuntimeException("could not load index scene fxml");
        }

        FXMLLoader rootSceneLoader = new FXMLLoader(rootSceneURL);

        Parent root = rootSceneLoader.load();
        IndexController indexController = rootSceneLoader.getController();
        indexController.setPrimaryStage(primaryStage);
        indexController.setClient(client);
        indexController.setUsername(username);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
}

