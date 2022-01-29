package password.vault.server.exceptions.user.repository;

public class InvalidUsernameException extends RegisterException {
    public InvalidUsernameException(String message) {
        super(message);
    }

    public InvalidUsernameException() {
    }
}
