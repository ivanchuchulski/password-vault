package password.vault.api;

public class CredentialDTO extends CredentialIdentifierDTO {
    private final String password;

    public CredentialDTO(String website, String usernameForWebsite, String password) {
        super(website, usernameForWebsite);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
