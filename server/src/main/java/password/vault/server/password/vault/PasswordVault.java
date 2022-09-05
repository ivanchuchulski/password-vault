package password.vault.server.password.vault;

import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.db.DatabaseConnectorException;

import java.util.List;

public interface PasswordVault {
    void addPassword(String username, WebsiteCredential websiteCredential, String masterPassword)
            throws CredentialsAlreadyAddedException, PasswordEncryptor.PasswordEncryptorException, DatabaseConnectorException;

    void removePassword(String username, String website, String usernameForSite, String masterPassword)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException, DatabaseConnectorException,
            CredentialRemovalException;

    String retrieveCredentials(String username, String website, String usernameForSite, String masterPassword)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException,
            PasswordEncryptor.PasswordEncryptorException,
            DatabaseConnectorException;

    List<CredentialIdentifier> getAllCredentials(String username) throws UsernameNotHavingCredentialsException,
            DatabaseConnectorException;

    boolean userHasCredentialsForSiteAndUsername(String username, String website, String usernameForSite) throws
            DatabaseConnectorException;

    class CredentialNotFoundException extends Exception {
        public CredentialNotFoundException(String message) {
            super(message);
        }

        public CredentialNotFoundException() {

        }
    }

    class UsernameNotHavingCredentialsException extends Exception {
        public UsernameNotHavingCredentialsException(String message) {
            super(message);
        }

        public UsernameNotHavingCredentialsException() {

        }
    }

    class CredentialRemovalException extends Exception {

        public CredentialRemovalException(String message) {
            super(message);
        }
    }

    class CredentialsAlreadyAddedException extends Exception {
        public CredentialsAlreadyAddedException(String message) {
            super(message);
        }

        public CredentialsAlreadyAddedException() {

        }
    }
}
