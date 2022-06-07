package password.vault.client.gui.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import password.vault.api.CredentialIdentifierDTO;
import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.Context;
import password.vault.client.gui.FXMLScenes;
import password.vault.client.gui.StageManager;
import password.vault.client.gui.model.CredentialAdditionRequest;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

// TODO : handle session expiration
public class IndexController {
    private final Client client;

    private final Gson gson;
    private final String username;

    @FXML
    private Button btnLogout;

    @FXML
    private FlowPane flowPane;

    @FXML
    private Label lblWelcome;

    @FXML
    private Button btnAddCredential;

    public IndexController() {
        Context context = Context.getInstance();
        this.client = context.getClient();
        this.username = context.getLoggedInUsername();

        this.gson = new Gson();
    }

    @FXML
    void initialize() {
        lblWelcome.setText(lblWelcome.getText() + username + "!");

        Response response = fetchCredentials();

        if (response == null) {
            return;
        }

        if (!response.serverResponse().equals(ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS)) {
            CommonUIElements.getErrorAlert("error fetching data from server" + response.message());
            return;
        }

        Type listType = new TypeToken<List<CredentialIdentifierDTO>>() {
        }.getType();
        List<CredentialIdentifierDTO> credentials = gson.fromJson(response.message(), listType);


        List<Credential> guiCredentials = new LinkedList<>();
        for (CredentialIdentifierDTO credential : credentials) {
            Credential guiCredential = new Credential();
            guiCredential.setIndexController(this);
            guiCredential.setCredentialIdentifierDTO(credential);

            guiCredentials.add(guiCredential);
        }

        flowPane.setOrientation(Orientation.VERTICAL);
        flowPane.setVgap(10);
        flowPane.setHgap(10);
        flowPane.getChildren().addAll(guiCredentials);
    }

    @FXML
    void btnAddCredentialClicked(ActionEvent event) {
        testCustomDialogPane();
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

    public void fetchPassword(CredentialIdentifierDTO credentialIdentifierDTO, String masterPassword) {
        try {
            client.sendRequest(ServerTextCommandsFactory.retrieveCredentials(credentialIdentifierDTO.getWebsite(),
                                                                             credentialIdentifierDTO.getUsernameForWebsite(),
                                                                             masterPassword));
            Response response = client.receiveResponse();

            checkResponseForValidSession(response);

            if (!response.serverResponse().equals(ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS)) {
                CommonUIElements.getErrorAlert(response.message()).showAndWait();
                return;
            }

            String cleartextPassword = response.message();
            copyPasswordToClipboard(cleartextPassword);
            CommonUIElements.getInformationAlert("Credential retrieval success!", "").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getErrorAlert("error fetching data from server").showAndWait();
        }
    }

    public void removePassword(CredentialIdentifierDTO credentialIdentifierDTO, String masterPassword) {
        CommonUIElements.getErrorAlert("Not implemented yet!").showAndWait();
    }

    private void switchToLoginScene() {
        Context context = Context.getInstance();

        context.setLoggedInUsername("");
        StageManager stageManager = context.getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
    }

    private Response fetchCredentials() {
        try {
            client.sendRequest(ServerTextCommandsFactory.getAllCredentialsJSON());
            return client.receiveResponse();
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getErrorAlert("error fetching data from server");
            return null;
        }
    }

    private void checkResponseForValidSession(Response response) {
        if (response.serverResponse().equals(ServerResponses.SESSION_EXPIRED)) {
            CommonUIElements.getInformationAlert("Your session has expired, please log in again", "").showAndWait();
            switchToLoginScene();
        }
    }

    private void copyPasswordToClipboard(String cleartextPassword) {
        ClipboardContent content = new ClipboardContent();
        content.putString(cleartextPassword);

        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    private void testCustomDialogPane() {

        AddCredentialDialogPaneController addCredentialDialogPaneController =
                new AddCredentialDialogPaneController(Context.getInstance().getStageManager().getCurrentStage());

        Optional<CredentialAdditionRequest> credentialAdditionRequestOptional =
                addCredentialDialogPaneController.showAndWait();

        credentialAdditionRequestOptional.ifPresent(credentialAdditionRequest -> System.out.println(credentialAdditionRequest));
    }
}
