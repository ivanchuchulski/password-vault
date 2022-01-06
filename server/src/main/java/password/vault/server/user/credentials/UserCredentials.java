package password.vault.server.user.credentials;

import password.vault.server.credential.CredentialIdentifier;

import java.util.HashMap;
import java.util.Map;

public class UserCredentials {
//    { website, usernameForWebsite -> password }
    private final Map<CredentialIdentifier, String> websitesCredentials;

    public UserCredentials() {
        websitesCredentials = new HashMap<>();
    }

    public boolean containsCredential(CredentialIdentifier credentialIdentifier) {
        return websitesCredentials.containsKey(credentialIdentifier);
    }

    public void addCredential(CredentialIdentifier credentialIdentifier, String encryptedPassword) {
        websitesCredentials.put(credentialIdentifier, encryptedPassword);
    }

    public void removeCredential(CredentialIdentifier credentialIdentifier) {
        websitesCredentials.remove(credentialIdentifier);
    }

    public String getEncryptedPassword(CredentialIdentifier credentialIdentifier) {
        return websitesCredentials.get(credentialIdentifier);
    }
}
