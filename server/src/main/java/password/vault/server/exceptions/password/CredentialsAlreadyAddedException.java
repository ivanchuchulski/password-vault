package password.vault.server.exceptions.password;

public class CredentialsAlreadyAddedException extends Exception {
    public CredentialsAlreadyAddedException(String message) {
        super(message);
    }
}
