package password.vault.server.password.vault;

import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.db.DatabaseConnectorException;

import java.util.List;

public class PasswordVaultDB implements PasswordVault {
    private final DatabaseConnector databaseConnector;

    public PasswordVaultDB(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @Override
    public void addPassword(String username, WebsiteCredential websiteCredential, String masterPassword) throws
            CredentialsAlreadyAddedException, PasswordEncryptor.PasswordEncryptorException,
            DatabaseConnectorException {
        if (databaseConnector.isCredentialAdded(username, websiteCredential.getCredentialIdentifier())) {
            throw new CredentialsAlreadyAddedException();
        }

        EncryptedPassword encryptedPassword =
                PasswordEncryptor.encryptWithMasterPassword(websiteCredential.password(), masterPassword);

        databaseConnector.insertCredential(username, websiteCredential.website(), websiteCredential.usernameForSite()
                , encryptedPassword);
    }

    @Override
    public void removePassword(String username, String website, String usernameForSite, String masterPassword) throws
            UsernameNotHavingCredentialsException, CredentialNotFoundException, DatabaseConnectorException,
            CredentialRemovalException {
        CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);
        if (!databaseConnector.doesUserHaveAnyCredentials(username)) {
            throw new UsernameNotHavingCredentialsException();
        }

        if (!databaseConnector.isCredentialAdded(username, credentialIdentifier)) {
            throw new CredentialNotFoundException();
        }

        boolean removalSuccess = databaseConnector.deleteCredential(username, new CredentialIdentifier(website,
                                                                                                       usernameForSite));
        if (!removalSuccess) {
            throw new CredentialRemovalException("unable to remove credentials");
        }
    }

    @Override
    public String retrieveCredentials(String username, String website, String usernameForSite, String masterPassword) throws
            UsernameNotHavingCredentialsException, CredentialNotFoundException,
            PasswordEncryptor.PasswordEncryptorException,
            DatabaseConnectorException {
        CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);

        if (!databaseConnector.doesUserHaveAnyCredentials(username)) {
            throw new UsernameNotHavingCredentialsException();
        }

        if (!databaseConnector.isCredentialAdded(username, credentialIdentifier)) {
            throw new CredentialNotFoundException();
        }

        EncryptedPassword encryptedPassword = databaseConnector.getCredential(username, credentialIdentifier);

        return PasswordEncryptor.decryptWithMasterPassword(encryptedPassword, masterPassword);
    }

    @Override
    public List<CredentialIdentifier> getAllCredentials(String username) throws UsernameNotHavingCredentialsException,
            DatabaseConnectorException {
        if (!databaseConnector.doesUserHaveAnyCredentials(username)) {
            throw new UsernameNotHavingCredentialsException();
        }

        return databaseConnector.getAllCredentialsForUser(username);
    }


    @Override
    public boolean userHasCredentialsForSiteAndUsername(String username, String website, String usernameForSite) throws
            DatabaseConnectorException {
        return databaseConnector.isCredentialAdded(username, new CredentialIdentifier(website, usernameForSite));
    }

}
