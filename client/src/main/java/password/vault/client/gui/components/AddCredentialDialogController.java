package password.vault.client.gui.components;

import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import password.vault.client.gui.controllers.YesNoCheck;
import password.vault.client.gui.model.AddCredentialDialogResult;
import password.vault.client.gui.model.FieldConstraints;

import java.io.IOException;

/**
 * source : <a href="https://stackoverflow.com/a/64967696/9127495">...</a>
 */
public class AddCredentialDialogController extends Dialog<AddCredentialDialogResult> {

    private static final String ADD_CREDENTIALS_DIALOG_FILENAME = "/password/vault/client/gui/components" +
            "/add_credential_dialog_pane.fxml";
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

    public AddCredentialDialogController(Window owner) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ADD_CREDENTIALS_DIALOG_FILENAME));
            fxmlLoader.setController(this);

            fxmlLoader.load();

            setDialogPane(dialogAddCredentials);
            setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {

                    return new AddCredentialDialogResult(txtWebsite.getText(),
                                                         txtUsername.getText(),
                                                         txtPassword.getText(),
                                                         choiceSecurityCheck.getSelectionModel().getSelectedItem());
                }
                return null;
            });

            final Button okButton = (Button) dialogAddCredentials.lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, actionEventFilter -> {
                if (!fieldsAreValid()) {
                    lblErrors.setVisible(true);
                    lblErrors.setText("error : all fields are necessary!");
                    actionEventFilter.consume();
                    return;
                }

                if (!isUsernameValid()) {
                    lblErrors.setVisible(true);
                    lblErrors.setText("error : username is not valid");
                    actionEventFilter.consume();
                    return;
                }

                if (!isWebsiteValid()) {
                    lblErrors.setVisible(true);
                    lblErrors.setText("error : website is not valid");
                    actionEventFilter.consume();
                    return;
                }
            });

            initOwner(owner);
            initModality(Modality.APPLICATION_MODAL);
            setOnShowing(dialogEvent -> Platform.runLater(() -> txtWebsite.requestFocus()));
        } catch (IOException exception) {
            throw new RuntimeException("unable to load custom dialog fxml file", exception);
        }
    }

    @FXML
    void initialize() {
        CommonUIElements.setupShowHidePasswordCheckbox(txtPasswordShown, txtPassword, chBoxShowPassword);

        ObservableList<YesNoCheck> items = choiceSecurityCheck.getItems();
        items.add(YesNoCheck.NO);
        items.add(YesNoCheck.YES);
        choiceSecurityCheck.getSelectionModel().selectFirst();
    }

    private boolean fieldsAreValid() {
        return !txtWebsite.getText().isBlank() && !txtUsername.getText().isBlank() && !txtPassword.getText().isBlank();
    }

    private boolean isWebsiteValid() {
        return txtWebsite.getText().matches(FieldConstraints.WEBSITE_PATTERN);
    }

    private boolean isUsernameValid() {
        return txtUsername.getText().matches(FieldConstraints.VALID_USERNAME_PATTERN);
    }

}
