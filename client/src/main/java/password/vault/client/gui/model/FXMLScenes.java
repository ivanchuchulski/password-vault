package password.vault.client.gui.model;

import java.net.URL;

public enum FXMLScenes {
    LOGIN("/password/vault/client/gui/controllers/login.fxml"),
    REGISTRATION("/password/vault/client/gui/controllers/registration.fxml"),
    INDEX("/password/vault/client/gui/controllers/index.fxml");

    private final String fxmlFilename;
    FXMLScenes(String fxmlFilename) {
        this.fxmlFilename = fxmlFilename;
    }

    public String getFxmlFilename() {
        return fxmlFilename;
    }

    public URL getFileURL() {
        return getClass().getResource(getFxmlFilename());
    }
}
