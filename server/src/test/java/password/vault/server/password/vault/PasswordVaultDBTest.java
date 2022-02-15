package password.vault.server.password.vault;

import org.junit.Test;
import org.mockito.Mockito;
import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.db.DatabaseConnector;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PasswordVaultDBTest {
    @Test
    public void testAddingAPasswordAndRetrievingItReturnsTheSamePassword() throws Exception {
        DatabaseConnector databaseConnectorMock = Mockito.mock(DatabaseConnector.class);
        PasswordVault passwordVault = new PasswordVaultDB(databaseConnectorMock);

        String user = "peter jackson";
        String masterPassword = "jackson1234";
        WebsiteCredential websiteCredential = new WebsiteCredential("facebook.com", "peter", "pass1234");

        EncryptedPassword encryptedPassword =
                PasswordEncryptor.encryptWithMasterPassword(websiteCredential.password(), masterPassword);

        when(databaseConnectorMock.insertCredential(user, websiteCredential.website(),
                                                    websiteCredential.usernameForSite(), encryptedPassword)).thenReturn(true);

        passwordVault.addPassword(user, websiteCredential, masterPassword);

        when(databaseConnectorMock.doesUserHaveAnyCredentials(user)).thenReturn(true);
        when(databaseConnectorMock.isCredentialAdded(user, websiteCredential.getCredentialIdentifier())).thenReturn(true);
        when(databaseConnectorMock.getCredential(user, websiteCredential.getCredentialIdentifier())).thenReturn(encryptedPassword);

        String retrievedPassword = passwordVault.retrieveCredentials(user, websiteCredential.website(),
                                                                     websiteCredential.usernameForSite(),
                                                                     masterPassword);

        assertEquals("the retrieved password should match the added one", websiteCredential.password(),
                     retrievedPassword);

    }

}