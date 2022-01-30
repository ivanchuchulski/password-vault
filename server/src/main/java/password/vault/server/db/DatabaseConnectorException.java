package password.vault.server.db;

public class DatabaseConnectorException extends Exception {
    public DatabaseConnectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseConnectorException(String message) {
        super(message);
    }
}
