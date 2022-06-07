package password.vault.client.gui.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import password.vault.api.CredentialIdentifierDTO;
import password.vault.client.gui.CommonUIElements;

import java.io.IOException;
import java.util.Optional;

public class Credential extends VBox {

    private IndexController indexController;

    private CredentialIdentifierDTO credentialIdentifierDTO;

    @FXML
    private Label lblDomain;

    @FXML
    private Label lblUsername;

    @FXML
    private Button btnCopyPassword;

    @FXML
    private Button btnRemovePassword;


    public Credential() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/password/vault/client/gui/controllers" +
                                                                                  "/credential.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);

            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException("unable to load custom vbox class", exception);
        }
    }

    public void setIndexController(IndexController indexController) {
        this.indexController = indexController;
    }

    public void setCredentialIdentifierDTO(CredentialIdentifierDTO credentialIdentifierDTO) {
        this.credentialIdentifierDTO = credentialIdentifierDTO;

        lblDomain.setText(credentialIdentifierDTO.getWebsite());
        lblUsername.setText(credentialIdentifierDTO.getUsernameForWebsite());
    }

    public Label getLblDomain() {
        return lblDomain;
    }

    public Label getLblUsername() {
        return lblUsername;
    }

    @Override
    public String toString() {
        return "Credential{" +
                "lblDomain=" + lblDomain +
                ", lblUsername=" + lblUsername +
                '}';
    }

    @FXML
    void btnCopyPasswordClicked(ActionEvent event) {
        TextInputDialog textInputDialog = CommonUIElements.getMasterPasswordDialog();
        // Dialog<String> customPasswordDialog = getCustomPasswordDialog();

        Optional<String> result = textInputDialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String enteredMasterPassword = result.get();
        indexController.fetchPassword(credentialIdentifierDTO, enteredMasterPassword);
    }

    @FXML
    void btnRemovePasswordClicked(ActionEvent event) {
        TextInputDialog textInputDialog = CommonUIElements.getMasterPasswordDialog();
        // Dialog<String> customPasswordDialog = getCustomPasswordDialog();

        Optional<String> result = textInputDialog.showAndWait();

        if (result.isEmpty()) {
            return;
        }

        String enteredMasterPassword = result.get();
        indexController.removePassword(credentialIdentifierDTO, enteredMasterPassword);
    }

    @FXML
    void credentialSelected(MouseEvent event) {
        System.out.println("selected node");
        System.out.println(this);

        this.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID,
                                                   CornerRadii.EMPTY,
                                                   BorderWidths.DEFAULT)));
    }
}
