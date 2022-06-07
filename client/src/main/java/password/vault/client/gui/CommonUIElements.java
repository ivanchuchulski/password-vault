package password.vault.client.gui;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

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

    public static TextInputDialog getMasterPasswordDialog() {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("");
        textInputDialog.setHeaderText("Enter your Master password");
        textInputDialog.setContentText("Master password:");
        return textInputDialog;
    }

    /**
     * <a href="source">: https://stackoverflow.com/a</a>/53825771
     *
     * @return custom text dialog where the input text from the user is hidden
     */
    public static Dialog<String> getCustomPasswordDialog() {
        Dialog<String> dialog = new Dialog<>();

        dialog.setTitle("");
        dialog.setHeaderText("Enter your Master password");
        dialog.setGraphic(new Circle(15, Color.RED)); // Custom graphic
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label("Master password:"), pwd);
        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwd.getText();
            }
            return null;
        });

        return dialog;
    }
}
