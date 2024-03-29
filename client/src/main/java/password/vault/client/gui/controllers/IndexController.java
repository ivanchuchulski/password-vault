package password.vault.client.gui.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import password.vault.api.CredentialIdentifierDTO;
import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.api.ServerTextCommandsFactory;
import password.vault.client.communication.Client;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.components.AddCredentialDialogController;
import password.vault.client.gui.components.GenerateCredentialDialogController;
import password.vault.client.gui.context.Context;
import password.vault.client.gui.context.StageManager;
import password.vault.client.gui.dto.AddCredentialRequestDTO;
import password.vault.client.gui.model.AddCredentialDialogResult;
import password.vault.client.gui.model.FXMLScenes;
import password.vault.client.gui.model.GenerateCredentialsDialogResult;
import password.vault.client.gui.model.YesNoCheck;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class IndexController {
    private final Client client;

    private final Gson gson;
    private final String username;

    private List<Credential> currentCredentials;

    @FXML
    private Button btnLogout;

    @FXML
    private ScrollPane sclPane;

    @FXML
    private FlowPane flowPane;

    @FXML
    private Label lblWelcome;

    @FXML
    private TextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnAddCredential;

    @FXML
    private Button btnGenerateCredential;

    @FXML
    private Button btnCheckPassword;

    @FXML
    private Label lblErrors;

    public IndexController() {
        Context context = Context.getInstance();
        this.client = context.getClient();
        this.username = context.getLoggedInUsername();

        this.gson = new Gson();

        this.currentCredentials = new LinkedList<>();
    }

    @FXML
    void initialize() {
        lblWelcome.setText(lblWelcome.getText() + username + "!");

        flowPane.setVgap(10);
        flowPane.setHgap(10);

        sclPane.setFitToWidth(true);
        sclPane.setFitToHeight(true);

        getCredentialForUser();
    }

    @FXML
    void btnAddCredentialClicked(ActionEvent event) {
        Dialog<AddCredentialDialogResult> addCredentialDialogPaneController =
                new AddCredentialDialogController(Context.getInstance().getStageManager().getCurrentStage());

        Optional<AddCredentialDialogResult> credentialAdditionRequestOptional =
                addCredentialDialogPaneController.showAndWait();

        if (credentialAdditionRequestOptional.isEmpty()) {
            return;
        }

        Dialog<String> getPasswordDialogController = CommonUIElements.getMasterPasswordDialog();
        Optional<String> masterPasswordOptional = getPasswordDialogController.showAndWait();
        if (masterPasswordOptional.isEmpty()) {
            CommonUIElements.getErrorAlert("Please enter your master password to proceed!").showAndWait();
            return;
        }

        AddCredentialDialogResult addCredentialDialogResult = credentialAdditionRequestOptional.get();
        String masterPassword = masterPasswordOptional.get();

        AddCredentialRequestDTO addCredentialRequestDTO =
                new AddCredentialRequestDTO(addCredentialDialogResult.website(),
                                            addCredentialDialogResult.username(),
                                            addCredentialDialogResult.password(),
                                            masterPassword);

        String command;
        if (addCredentialDialogResult.yesNoCheck().equals(YesNoCheck.YES)) {
            command = ServerTextCommandsFactory.addPasswordWithCheck(addCredentialRequestDTO.website(),
                                                                     addCredentialRequestDTO.username(),
                                                                     addCredentialRequestDTO.password(),
                                                                     addCredentialRequestDTO.masterPassword());
        } else {
            command = ServerTextCommandsFactory.addPasswordWithoutCheck(addCredentialRequestDTO.website(),
                                                                        addCredentialRequestDTO.username(),
                                                                        addCredentialRequestDTO.password(),
                                                                        addCredentialRequestDTO.masterPassword());
        }

        try {
            client.sendRequest(command);
            Response response = client.receiveResponse();

            checkResponseForValidSession(response);

            CommonUIElements.getInformationAlert(response.message(), "credential addition").showAndWait();

            if (response.serverResponse().equals(ServerResponses.CREDENTIAL_ADDITION_SUCCESS)) {
                getCredentialForUser();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert();
        }
    }

    @FXML
    void btnCheckPasswordClicked(ActionEvent event) {
        Dialog<String> passwordToCheckDialog = CommonUIElements.getPasswordToCheckDialog();
        Optional<String> passwordOptional = passwordToCheckDialog.showAndWait();

        if (passwordOptional.isEmpty()) {
            return;
        }

        String passwordToCheck = passwordOptional.get();
        try {
            String command = ServerTextCommandsFactory.checkPasswordSafetyCommand(passwordToCheck);
            client.sendRequest(command);
            Response response = client.receiveResponse();

            checkResponseForValidSession(response);

            CommonUIElements.getInformationAlert(response.message(), "password check").showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert();
        }
    }

    @FXML
    void btnGenerateCredentialClicked(ActionEvent event) {
        GenerateCredentialDialogController generateCredentialDialogController =
                new GenerateCredentialDialogController(Context
                                                              .getInstance()
                                                              .getStageManager()
                                                              .getCurrentStage());

        Optional<GenerateCredentialsDialogResult> generateCredentialsDialogResultOptional =
                generateCredentialDialogController.showAndWait();

        if (generateCredentialsDialogResultOptional.isEmpty()) {
            return;
        }

        Dialog<String> getPasswordDialogController = CommonUIElements.getMasterPasswordDialog();
        Optional<String> masterPasswordOptional = getPasswordDialogController.showAndWait();
        if (masterPasswordOptional.isEmpty()) {
            CommonUIElements.getErrorAlert("Please enter your master password to proceed!").showAndWait();
            return;
        }

        String masterPassword = masterPasswordOptional.get();
        GenerateCredentialsDialogResult generateCredentialsDialogResult = generateCredentialsDialogResultOptional.get();

        try {
            String command = ServerTextCommandsFactory.generatePassword(generateCredentialsDialogResult.website(),
                                                                        generateCredentialsDialogResult.username(),
                                                                        generateCredentialsDialogResult.passwordLength(), masterPassword);

            client.sendRequest(command);
            Response response = client.receiveResponse();

            checkResponseForValidSession(response);

            CommonUIElements.getInformationAlert(response.message(), "password generation").showAndWait();

            if (response.serverResponse().equals(ServerResponses.CREDENTIAL_GENERATION_SUCCESS)) {
                getCredentialForUser();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert();
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

    @FXML
    void btnSearchClicked(ActionEvent event) {
        String searchText = txtSearch.getText();

        if (searchText.isBlank()) {
            return;
        }

        ObservableList<Node> flowPaneChildren = flowPane.getChildren();

        List<Credential> filtered = new LinkedList<>();
        for (Credential credential : currentCredentials) {
            String usernameFormatted = credential.getLblUsername().getText().toLowerCase();
            String websiteFormatted = credential.getLblDomain().getText().toLowerCase();

            if (usernameFormatted.contains(searchText) || websiteFormatted.contains(searchText)) {
                filtered.add(credential);
            }
        }

        flowPaneChildren.clear();

        if (filtered.isEmpty()) {
            lblErrors.setVisible(true);
            lblErrors.setText("No items have matched your criteria!");
        } else {
            flowPaneChildren.addAll(filtered);
        }
    }

    @FXML
    void btnClearClicked(ActionEvent event) {
        txtSearch.clear();

        lblErrors.setVisible(false);
        lblErrors.setText("");

        ObservableList<Node> flowPaneChildren = flowPane.getChildren();
        flowPaneChildren.clear();
        flowPaneChildren.addAll(currentCredentials);
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
        try {
            String request = ServerTextCommandsFactory.removePassword(credentialIdentifierDTO.getWebsite(),
                                                                      credentialIdentifierDTO.getUsernameForWebsite(),
                                                                      masterPassword);
            client.sendRequest(request);
            Response response = client.receiveResponse();

            checkResponseForValidSession(response);

            CommonUIElements.getInformationAlert(response.message(), "credential removal").showAndWait();

            if (response.serverResponse().equals(ServerResponses.CREDENTIAL_REMOVAL_SUCCESS)) {
                getCredentialForUser();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getFailedRequestWarningAlert().showAndWait();
        }
    }

    private void getCredentialForUser() {
        try {
            client.sendRequest(ServerTextCommandsFactory.getAllCredentialsJSON());
            Response response = client.receiveResponse();

            ObservableList<Node> flowPaneChildren = flowPane.getChildren();
            flowPaneChildren.clear();

            if (!response.serverResponse().equals(ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS)) {
                lblErrors.setVisible(true);
                lblErrors.setText(response.message());
                return;
            }

            Type listType = new TypeToken<List<CredentialIdentifierDTO>>() {
            }.getType();
            List<CredentialIdentifierDTO> credentials = gson.fromJson(response.message(), listType);

            lblErrors.setVisible(false);
            lblErrors.setText("");

            currentCredentials.clear();

            for (CredentialIdentifierDTO credential : credentials) {
                Credential guiCredential = new Credential();
                guiCredential.setIndexController(this);
                guiCredential.setCredentialIdentifierDTO(credential);

                currentCredentials.add(guiCredential);
            }

            flowPaneChildren.addAll(currentCredentials);

            // for testing purposes add a bunch of dummy credentials
            // flowPaneChildren.addAll(getDummyCredentials());

            if (!txtSearch.getText().isBlank()) {
                btnSearch.fire();
            }
        } catch (IOException e) {
            e.printStackTrace();
            CommonUIElements.getErrorAlert("error fetching data from server");
        }
    }

    private List<Credential> getDummyCredentials() {
        int limit = 5;
        String websiteTest = "website_";
        String domain = "domain_";

        LinkedList<Credential> dummyCredentials = new LinkedList<>();
        for (int i = 0; i < limit; i++) {
            CredentialIdentifierDTO credentialIdentifierDTO = new CredentialIdentifierDTO(websiteTest + i, domain + i);

            Credential guiCredential = new Credential();
            guiCredential.setIndexController(this);
            guiCredential.setCredentialIdentifierDTO(credentialIdentifierDTO);

            dummyCredentials.add(guiCredential);
        }

        return dummyCredentials;
    }

    private void switchToLoginScene() {
        Context context = Context.getInstance();

        context.setLoggedInUsername("");
        StageManager stageManager = context.getStageManager();
        stageManager.switchScene(FXMLScenes.LOGIN);
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

}
