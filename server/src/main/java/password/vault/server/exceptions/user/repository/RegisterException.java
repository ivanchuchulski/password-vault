package password.vault.server.exceptions.user.repository;

public class RegisterException extends Exception {
    public RegisterException(String message) {
        super(message);
    }

    public RegisterException() {
    }
}
