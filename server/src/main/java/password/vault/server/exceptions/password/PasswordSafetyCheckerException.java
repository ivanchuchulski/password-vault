package password.vault.server.exceptions.password;

public class PasswordSafetyCheckerException extends Exception {
    public PasswordSafetyCheckerException(String message) {
        super(message);
    }

    public PasswordSafetyCheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
