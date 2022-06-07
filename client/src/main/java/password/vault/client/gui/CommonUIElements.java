package password.vault.client.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CommonUIElements {
    private static Alert getAlertMessage(Alert.AlertType type, String header, String context, String title) {
        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);

        return alert;
    }

    public static Alert getQuitAlert() {
        return getAlertMessage(Alert.AlertType.CONFIRMATION,
                               "Quitting Password Vault",
                               "Are you sure you want to exit?",
                               "Quit confirmation");
    }

    public static Alert getInformationAlert(String text, String context) {
        return getAlertMessage(Alert.AlertType.INFORMATION, text, context, "");
    }

    public static Alert getErrorAlert(String text) {
        return getAlertMessage(Alert.AlertType.ERROR, text, "", "");
    }

    public static Alert getFailedRequestWarningAlert() {
        return getAlertMessage(Alert.AlertType.WARNING, "Couldn't complete " +
                "your request!", "", "");
    }


    /**
     * source : <a href="https://stackoverflow.com/a/17014524/9127495">...</a>
     */
    public static void setupShowHidePasswordCheckbox(TextField textField, PasswordField passwordField,
                                               CheckBox showCheckbox) {
        textField.setVisible(false);
        textField.setManaged(false);
        textField.textProperty().bindBidirectional(passwordField.textProperty());

        textField.managedProperty().bind(showCheckbox.selectedProperty());
        textField.visibleProperty().bind(showCheckbox.selectedProperty());

        passwordField.managedProperty().bind(showCheckbox.selectedProperty().not());
        passwordField.visibleProperty().bind(showCheckbox.selectedProperty().not());
    }
}
