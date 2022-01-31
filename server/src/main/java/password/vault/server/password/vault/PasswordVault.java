package password.vault.server.password.vault;

import password.vault.server.exceptions.InvalidUsernameForSiteException;
import password.vault.server.exceptions.InvalidWebsiteException;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.CredentialsAlreadyAddedException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.exceptions.password.UsernameNotHavingCredentialsException;

public interface PasswordVault {
    void addPassword(String username, String website, String usernameForSite, String password)
            throws CredentialsAlreadyAddedException, PasswordEncryptorException,
            InvalidUsernameForSiteException, InvalidWebsiteException;

    void removePassword(String username, String website, String usernameForSite)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException;

    String retrieveCredentials(String username, String website, String usernameForSite)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException, PasswordEncryptorException;

    boolean userHasCredentialsForSiteAndUsername(String username, String website, String usernameForSite);
}
