package password.vault.server.password.vault;

import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.CredentialsAlreadyAddedException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.exceptions.password.UsernameNotHavingCredentialsException;

public interface PasswordVault {
    void addPassword(String username, WebsiteCredential websiteCredential, String masterPassword)
            throws CredentialsAlreadyAddedException, PasswordEncryptorException, DatabaseConnectorException;

    void removePassword(String username, String website, String usernameForSite, String masterPassword)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException, DatabaseConnectorException,
            CredentialRemovalFailure;

    String retrieveCredentials(String username, String website, String usernameForSite, String masterPassword)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException, PasswordEncryptorException,
            DatabaseConnectorException;

    boolean userHasCredentialsForSiteAndUsername(String username, String website, String usernameForSite) throws
            DatabaseConnectorException;
}
