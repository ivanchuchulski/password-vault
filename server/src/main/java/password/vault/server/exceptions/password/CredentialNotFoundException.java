package password.vault.server.exceptions.password;

public class CredentialNotFoundException extends Exception {
    public CredentialNotFoundException(String message) {
        super(message);
    }
}
