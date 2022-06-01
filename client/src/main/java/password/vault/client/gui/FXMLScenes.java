package password.vault.client.gui;

import java.net.URL;

public enum FXMLScenes {
    LOGIN("login.fxml"),
    REGISTRATION("registration.fxml"),
    INDEX("index.fxml");

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
