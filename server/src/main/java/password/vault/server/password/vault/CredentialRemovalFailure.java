package password.vault.server.password.vault;

public class CredentialRemovalFailure extends Exception {
    public CredentialRemovalFailure() {
    }

    public CredentialRemovalFailure(String message) {
        super(message);
    }
}
