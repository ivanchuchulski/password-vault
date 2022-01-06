package password.vault.server.exceptions.password;

public class PasswordGeneratorException extends Exception {
    public PasswordGeneratorException(String message) {
        super(message);
    }

    public PasswordGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
