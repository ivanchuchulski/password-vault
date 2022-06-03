package password.vault.server.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import password.vault.server.Server;
import password.vault.server.ServerConfigurator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandPanel {

    public static final String SCENE_FXML_FILENAME = "/password/vault/server/gui/command_panel.fxml";

    private Server server;

    private final ExecutorService executorService;

    public CommandPanel() {
        executorService = Executors.newSingleThreadExecutor();
    }

    @FXML
    void initialize() {
        btnStop.setDisable(true);
    }


    @FXML
    private Button btnStart;

    @FXML
    private Button btnStop;

    @FXML
    private Label lblState;

    public void setServer(Server server) {
        this.server = server;
    }

    @FXML
    void btnStartClicked(ActionEvent event) {
        btnStart.setDisable(true);
        btnStop.setDisable(false);

        lblState.setText("RUNNING");

        if (!server.isAlive()) {
            server = ServerConfigurator.getServer();
        }

        executorService.execute(new ServerExecutorRunnable(server));

        getInformationAlert("started server").showAndWait();
    }

    @FXML
    void btnStopClicked(ActionEvent event) {
        btnStart.setDisable(false);
        btnStop.setDisable(true);

        lblState.setText("STOPPED");

        server.stop();

        getInformationAlert("stopped server").showAndWait();
    }

    private static Alert getInformationAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setHeaderText(text);

        return alert;
    }

}
