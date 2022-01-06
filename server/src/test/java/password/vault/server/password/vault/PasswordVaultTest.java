package password.vault.server.password.vault;

import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import password.vault.server.dto.CredentialDTO;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.CredentialsAlreadyAddedException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.exceptions.password.UsernameNotHavingCredentialsException;
import password.vault.server.cryptography.PasswordEncryptor;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PasswordVaultTest {
    private static final String SYSTEM_FILE_SEPARATOR = File.separator;

    private static final String SAMPLE_USERNAMES_FILEPATH = "test_resources" + File.separator + "unique-usernames.txt";
    private static final Random random = new Random();
    private static List<String> usernamesForTesting;

    private static final Path CREDENTIALS_FILE =
            Path.of("resources" + SYSTEM_FILE_SEPARATOR + "password-vault-test-credentials.txt");
    private static final Gson gson = new Gson();
    private static PasswordVault passwordVault;

    private static final String WEBSITE_FOR_TESTING = "facebook.com";
    private static final String ANOTHER_WEBSITE_FOR_TESTING = "twitter.com";
    private static final String PASSWORD_FOR_TESTING = "test_1234_password";

    private static final String INITIALLY_ADDED_USERNAME = "ivo";
    private static final String INITIALLY_ADDED_USERNAME_FOR_SITE =
            buildUsernameForSiteFromUsername(INITIALLY_ADDED_USERNAME);

    private static String uniqueUsername;
    private static String usernameForWebsite;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Files.deleteIfExists(CREDENTIALS_FILE);
        Files.createFile(CREDENTIALS_FILE);

        addOneCredentialToCredentialsFile();

        passwordVault = new PasswordVault(CREDENTIALS_FILE);

        usernamesForTesting = getUniqueNamesFromFile();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        Files.deleteIfExists(CREDENTIALS_FILE);
    }

    @Before
    public void setUp() {
        uniqueUsername = getUniqueUsername();
        usernameForWebsite = buildUsernameForSiteFromUsername(uniqueUsername);
    }

    @Test
    public void testInitiallyAddedCredentialFromFileExistsAndItsValid() throws Exception {
        assertTrue(passwordVault.userHasCredentialsForSiteAndUsername(INITIALLY_ADDED_USERNAME, WEBSITE_FOR_TESTING,
                                                                      INITIALLY_ADDED_USERNAME_FOR_SITE));

        String retrievedCredentials = passwordVault.retrieveCredentials(INITIALLY_ADDED_USERNAME, WEBSITE_FOR_TESTING,
                                                                        INITIALLY_ADDED_USERNAME_FOR_SITE);

        assertEquals("unexpected password for the initially added credential", PASSWORD_FOR_TESTING,
                     retrievedCredentials);
    }

    @Test
    public void testAddingAPasswordAndRetrievingItReturnsTheSamePassword() throws Exception {
        passwordVault.addPassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite, PASSWORD_FOR_TESTING);

        String retrievedPassword = passwordVault
                .retrieveCredentials(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite);

        assertEquals("the retrieved password should match the added one", PASSWORD_FOR_TESTING, retrievedPassword);
    }

    /*
    a CredentialsAlreadyAddedException is thrown when we try to add the same <website, usernameForWebsite> tuple for
    given username
     */
    @Test(expected = CredentialsAlreadyAddedException.class)
    public void testAddingAPasswordForTheSameSiteAndUsernameThrowsException() throws Exception {
        passwordVault.addPassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite, PASSWORD_FOR_TESTING);

        passwordVault.addPassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite, PASSWORD_FOR_TESTING);
    }

    @Test(expected = CredentialNotFoundException.class)
    public void testRemovingAPasswordRemovesIt() throws Exception {
        passwordVault.addPassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite, PASSWORD_FOR_TESTING);

        passwordVault.removePassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite);

        passwordVault.retrieveCredentials(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite);
    }

    @Test(expected = UsernameNotHavingCredentialsException.class)
    public void testRemovingAPasswordForNonAddedUser() throws Exception {
        passwordVault.removePassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite);
    }

    @Test(expected = CredentialNotFoundException.class)
    public void testRemovingAPasswordForNonAddedWebsiteAndUsername() throws Exception {
        passwordVault.addPassword(uniqueUsername, WEBSITE_FOR_TESTING, usernameForWebsite, PASSWORD_FOR_TESTING);

        passwordVault.removePassword(uniqueUsername, ANOTHER_WEBSITE_FOR_TESTING, usernameForWebsite);
    }

    private static List<String> getUniqueNamesFromFile() throws IOException {
        try (Reader reader = new FileReader(SAMPLE_USERNAMES_FILEPATH);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return bufferedReader.lines()
                                 .distinct()
                                 .collect(Collectors.toList());
        }
    }

    private static String getUniqueUsername() {
        return usernamesForTesting.remove(random.nextInt(usernamesForTesting.size()));
    }

    private static void addOneCredentialToCredentialsFile() throws PasswordEncryptorException {
        SecretKey secretKey = PasswordEncryptor
                .getKeyFromString(INITIALLY_ADDED_USERNAME + INITIALLY_ADDED_USERNAME_FOR_SITE);

        String encryptedPassword = PasswordEncryptor.encrypt(PASSWORD_FOR_TESTING, secretKey);

        CredentialDTO credentialDTO =
                new CredentialDTO(INITIALLY_ADDED_USERNAME, WEBSITE_FOR_TESTING,
                                  INITIALLY_ADDED_USERNAME_FOR_SITE, encryptedPassword);

        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(new FileWriter(String.valueOf(CREDENTIALS_FILE), true))) {

            String credentialAsJSON = gson.toJson(credentialDTO);

            bufferedWriter.write(credentialAsJSON);
            bufferedWriter.write(System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException ioException) {
            throw new RuntimeException("error : writing credential to file", ioException);
        }
    }

    private static String buildUsernameForSiteFromUsername(String username) {
        return String.format("%s_%s_1234", username, username);
    }
}