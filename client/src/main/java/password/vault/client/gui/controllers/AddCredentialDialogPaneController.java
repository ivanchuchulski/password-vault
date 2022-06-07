package password.vault.client.gui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Window;
import password.vault.client.gui.CommonUIElements;
import password.vault.client.gui.model.CredentialAdditionRequest;

import java.io.IOException;

/**
 * source : <a href="https://stackoverflow.com/a/64967696/9127495">...</a>
 */
public class AddCredentialDialogPaneController extends Dialog<CredentialAdditionRequest> {


    @FXML
    private DialogPane dialogAddCredentials;

    @FXML
    private GridPane gridData;

    @FXML
    private Label lblWebsite;

    @FXML
    private Label lblUsername;

    @FXML
    private Label lblPassword;

    @FXML
    private Label lblSecurityCheck;

    @FXML
    private TextField txtWebsite;

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtPasswordShown;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private CheckBox chBoxShowPassword;

    @FXML
    private ChoiceBox<YesNoCheck> choiceSecurityCheck;

    @FXML
    private Label lblErrors;

    public AddCredentialDialogPaneController(Window owner) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/password/vault/client/gui/controllers" +
                                                                                  "/add_credential_dialog_pane.fxml"));
            fxmlLoader.setController(this);

            fxmlLoader.load();

            setDialogPane(dialogAddCredentials);
            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {

                    return new CredentialAdditionRequest(txtWebsite.getText(),
                                                         txtUsername.getText(),
                                                         txtPassword.getText(),
                                                         choiceSecurityCheck.getSelectionModel().getSelectedItem());
                }
                return null;
            });

            final Button okButton = (Button) dialogAddCredentials.lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, ae -> {
                if (!fieldsAreValid()) {
                    lblErrors.setVisible(true);
                    lblErrors.setText("error : all fields are necessary!");
                    ae.consume(); //not valid
                }
            });

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setOnShowing(dialogEvent -> Platform.runLater(() -> txtWebsite.requestFocus()));

            choiceSecurityCheck.getItems().addAll(YesNoCheck.values());
            choiceSecurityCheck.getSelectionModel().selectFirst();

        } catch (IOException exception) {
            throw new RuntimeException("unable to load custom add credential class", exception);
        }
    }

    private boolean fieldsAreValid() {
        return !txtWebsite.getText().isBlank() && !txtUsername.getText().isBlank() && !txtPassword.getText().isBlank();
    }

    @FXML
    void initialize() {
        CommonUIElements.setupShowHidePasswordCheckbox(txtPasswordShown, txtPassword, chBoxShowPassword);
    }

    public DialogPane getDialogAddCredentials() {
        return dialogAddCredentials;
    }
}
