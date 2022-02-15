package password.vault.server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import password.vault.api.Response;
import password.vault.api.ServerCommand;
import password.vault.api.ServerResponses;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.cryptography.PasswordHash;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.user.repository.UserRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerTest {
    private static final int SERVER_PORT = 7778;

    private static Thread serverRunnerThread;
    private static Server server;
    private static TestClient client;

    private static UserRepository userRepository;
    private static PasswordVault passwordVault;

    private static final String SYSTEM_FILE_SEPARATOR = File.separator;
    private static final String SAMPLE_USERNAMES_FILEPATH =
            "test_resources" + SYSTEM_FILE_SEPARATOR + "unique-valid-usernames.txt";
    private static List<String> usernamesForTesting;
    private static Random random;

    private static final String PASSWORD_FOR_TESTING = "1234";
    private static final String MASTER_PASSWORD_FOR_TESTING = "4567";
    private static final String WEBSITE_FOR_TESTING = "facebook.com";
    private static final String MAIL_FOR_TESTING = "example@example.com";

    private static final int NUMBER_OF_GENERATED_SAFE_PASSWORDS = 1;

    private static final String SAMPLE_SAFE_PASSWORD = "_rrR~S>k$[8+Ps/x2WyaFv";
    private static final int SAFE_PASSWORD_LENGTH = SAMPLE_SAFE_PASSWORD.length();

    @BeforeClass
    public static void setUpBeforeClass() throws IOException, PasswordGenerator.PasswordGeneratorException,
            PasswordSafetyChecker.PasswordSafetyCheckerException {
        userRepository = Mockito.mock(UserRepository.class);
        passwordVault = Mockito.mock(PasswordVault.class);

        PasswordGeneratorResponse passwordGeneratorResponse = new PasswordGeneratorResponse(true,
                                                                                            NUMBER_OF_GENERATED_SAFE_PASSWORDS,
                                                                                            new String[]{SAMPLE_SAFE_PASSWORD});
        PasswordGenerator passwordGenerator = Mockito.mock(PasswordGenerator.class);
        when(passwordGenerator.generateSafePassword(anyInt())).thenReturn(passwordGeneratorResponse);

        PasswordSafetyResponse passwordSafetyResponse = new PasswordSafetyResponse(false, 0);
        PasswordSafetyChecker passwordSafetyChecker = Mockito.mock(PasswordSafetyChecker.class);
        when(passwordSafetyChecker.checkPassword(anyString())).thenReturn(passwordSafetyResponse);

        CommandExecutor commandExecutor = new CommandExecutor(userRepository, passwordVault, passwordSafetyChecker,
                                                              passwordGenerator);

        random = new Random();
        usernamesForTesting = getUniqueNamesFromFile();

        serverRunnerThread = new Thread(() -> {
            server = new Server(SERVER_PORT, commandExecutor);
            server.start();
        });

        serverRunnerThread.start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        serverRunnerThread.interrupt();
        server.stop();
    }

    @Before
    public void setUp() {
        client = new TestClient(SERVER_PORT);
    }

    @After
    public void tearDown() throws Exception {
        client.closeConnection();
    }

    @Test
    public void testServerResponseWithUnknownCommand() throws IOException {
        String unknownCommandText = "echo";

        Response actualResponse = sendRequestAndGetResponse(unknownCommandText);

        assertEquals("when sending unknown command an unknown command response should be received",
                     ServerResponses.UNKNOWN_COMMAND,
                     actualResponse.serverResponse());
    }

    @Test
    public void testServerResponseWhenRequestIsDisconnect() throws IOException {
        Response actualResponse = sendRequestAndGetResponse(disconnectCommand());

        assertEquals("when sending disconnect command a disconnect response should be received",
                     ServerResponses.DISCONNECTED,
                     actualResponse.serverResponse());
    }

    @Test
    public void testServerResponseToHelpCommand() throws IOException {
        Response actualResponse = sendRequestAndGetResponse(helpCommand());

        assertEquals("when sending help command an overview of all commands should be received",
                     ServerResponses.HELP_COMMAND,
                     actualResponse.serverResponse());

        assertEquals("when sending help command an overview of all commands should be received",
                     ServerCommand.printHelp(),
                     actualResponse.message());
    }

    @Test
    public void testServerResponseWhenCommandHasIncorrectNumberOfArguments() throws IOException {
        String command = disconnectCommand() + " me";
        Response actualResponse = sendRequestAndGetResponse(command);

        assertEquals("when sending disconnect command a disconnect response should be received",
                     ServerResponses.WRONG_COMMAND_NUMBER_OF_ARGUMENTS,
                     actualResponse.serverResponse());
    }

    @Test
    public void testValidRegistration() throws IOException {
        String username = getUniqueUsername();

        Response response = sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                                      PASSWORD_FOR_TESTING,
                                                                      MASTER_PASSWORD_FOR_TESTING,
                                                                      MASTER_PASSWORD_FOR_TESTING));

        assertEquals("valid registration should return a success response",
                     ServerResponses.REGISTRATION_SUCCESS,
                     response.serverResponse());
    }

    @Test
    public void testValidLogin() throws IOException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));

        Response response = sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        assertEquals("valid login should return a success response",
                     ServerResponses.LOGIN_SUCCESS,
                     response.serverResponse());
    }

    @Test
    public void testValidLogout() throws IOException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));

        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        Response response = sendRequestAndGetResponse(logoutCommand());

        System.out.println(response.message());
        assertEquals("valid logout should return a success response",
                     ServerResponses.LOGOUT_SUCCESS,
                     response.serverResponse());
    }

    @Test
    public void testLoggingInWhenAlreadyLoggedIn() throws IOException, UserRepository.UserNotFoundException,
            UserRepository.UserAlreadyLoggedInException, UserRepository.LoginException, PasswordHasher.HashException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        doThrow(UserRepository.UserAlreadyLoggedInException.class).when(userRepository)
                                                                  .logInUser(username, PASSWORD_FOR_TESTING);
        Response response = sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        assertEquals("logging in when user is already logged in should return a error response",
                     ServerResponses.USER_ALREADY_LOGGED,
                     response.serverResponse());
    }

    @Test
    public void testLogoutWithoutLoggingIn() throws IOException {
        Response response = sendRequestAndGetResponse(logoutCommand());

        assertEquals("logging out when a user has not logged in should return a error response",
                     ServerResponses.USER_NOT_LOGGED_IN,
                     response.serverResponse());
    }

    @Test
    public void testRegisteringWithWrongRepeatedPassword() throws IOException {
        String username = getUniqueUsername();

        Response response = sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                                      PASSWORD_FOR_TESTING + "1234",
                                                                      MASTER_PASSWORD_FOR_TESTING,
                                                                      MASTER_PASSWORD_FOR_TESTING));

        assertEquals("when trying to register with non matching passwords and error message should be returned",
                     ServerResponses.PASSWORD_DO_NOT_MATCH,
                     response.serverResponse());
    }

    @Test
    public void testAddingAPasswordAndRetrievingIt() throws IOException,
            PasswordEncryptor.PasswordEncryptorException, DatabaseConnectorException,
            PasswordVault.UsernameNotHavingCredentialsException, PasswordVault.CredentialNotFoundException,
            PasswordHasher.HashException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));

        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        when(userRepository.isUsernameLoggedIn(username)).thenReturn(true);
        PasswordHash masterPasswordHash = new PasswordHash(MASTER_PASSWORD_FOR_TESTING);
        when(userRepository.getMasterPassword(username)).thenReturn(masterPasswordHash);

        sendRequestAndGetResponse(addPasswordWithCheck(WEBSITE_FOR_TESTING, usernameForSite, PASSWORD_FOR_TESTING,
                                                       MASTER_PASSWORD_FOR_TESTING));


        when(passwordVault.retrieveCredentials(username, WEBSITE_FOR_TESTING, usernameForSite,
                                               MASTER_PASSWORD_FOR_TESTING)).thenReturn(PASSWORD_FOR_TESTING);
        Response response = sendRequestAndGetResponse(retrieveCredentials(WEBSITE_FOR_TESTING, usernameForSite,
                                                                          MASTER_PASSWORD_FOR_TESTING));

        sendRequestAndGetResponse(logoutCommand());

        assertEquals("incorrect response returned",
                     ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS,
                     response.serverResponse());

        assertEquals("the returned password should be the same as the provided one",
                     PASSWORD_FOR_TESTING,
                     response.message());
    }

    @Test
    public void testRemovingAPasswordRemovesIt() throws IOException, PasswordHasher.HashException,
            DatabaseConnectorException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        when(userRepository.isUsernameLoggedIn(username)).thenReturn(true);
        PasswordHash masterPasswordHash = new PasswordHash(MASTER_PASSWORD_FOR_TESTING);
        when(userRepository.getMasterPassword(username)).thenReturn(masterPasswordHash);

        sendRequestAndGetResponse(addPasswordWithCheck(WEBSITE_FOR_TESTING, usernameForSite, PASSWORD_FOR_TESTING,
                                                       MASTER_PASSWORD_FOR_TESTING));

        Response response = sendRequestAndGetResponse(removePassword(WEBSITE_FOR_TESTING, usernameForSite,
                                                                     MASTER_PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(logoutCommand());

        assertEquals("when removing a previously added password the response should be successful removal",
                     ServerResponses.CREDENTIAL_REMOVAL_SUCCESS,
                     response.serverResponse());
    }

    @Test
    public void testGeneratingAPasswordReturnsTheGeneratedOne() throws IOException, PasswordHasher.HashException,
            DatabaseConnectorException, PasswordEncryptor.PasswordEncryptorException,
            PasswordVault.UsernameNotHavingCredentialsException, PasswordVault.CredentialNotFoundException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, MAIL_FOR_TESTING, PASSWORD_FOR_TESTING,
                                                  PASSWORD_FOR_TESTING,
                                                  MASTER_PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING
        ));

        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        when(userRepository.isUsernameLoggedIn(username)).thenReturn(true);
        PasswordHash masterPasswordHash = new PasswordHash(MASTER_PASSWORD_FOR_TESTING);
        when(userRepository.getMasterPassword(username)).thenReturn(masterPasswordHash);

        Response generationResponse = sendRequestAndGetResponse(generatePassword(WEBSITE_FOR_TESTING, usernameForSite,
                                                                                 SAFE_PASSWORD_LENGTH,
                                                                                 MASTER_PASSWORD_FOR_TESTING));

        assertEquals("response should be success",
                     ServerResponses.CREDENTIAL_GENERATION_SUCCESS,
                     generationResponse.serverResponse());

        assertEquals("generated password should be the sample mocked password",
                     SAMPLE_SAFE_PASSWORD,
                     generationResponse.message());

        when(passwordVault.retrieveCredentials(username, WEBSITE_FOR_TESTING, usernameForSite,
                                               MASTER_PASSWORD_FOR_TESTING)).thenReturn(SAMPLE_SAFE_PASSWORD);

        Response response = sendRequestAndGetResponse(retrieveCredentials(WEBSITE_FOR_TESTING, usernameForSite,
                                                                          MASTER_PASSWORD_FOR_TESTING));
        assertEquals("response should be success",
                     ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS,
                     response.serverResponse());

        assertEquals("generated password should be the sample mocked password",
                     SAMPLE_SAFE_PASSWORD,
                     response.message());
    }

    @Test
    public void testAddingInPasswordFromNonLoggedInUser() throws IOException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        Response response = sendRequestAndGetResponse(addPasswordWithCheck(WEBSITE_FOR_TESTING, usernameForSite,
                                                                           PASSWORD_FOR_TESTING, MASTER_PASSWORD_FOR_TESTING));

        assertEquals("when trying to add a password for non-logged user an error response is expected",
                     ServerResponses.NOT_LOGGED_IN,
                     response.serverResponse());
    }

    private Response sendRequestAndGetResponse(String request) throws IOException {
        client.sendRequest(request);
        return client.receiveResponse();
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

    private static String buildUsernameForSiteFromUsername(String username) {
        return String.format("%s_%s_1234", username, username);
    }

    private String registerCommand(String username, String email, String password, String repeatedPassword,
                                   String masterPassword,
                                   String repeatedMasterPassword) {
        return String.format("%s %s %s %s %s %s %s",
                             ServerCommand.REGISTER.getCommandText(), username, email, password, repeatedPassword,
                             masterPassword, repeatedMasterPassword);
    }

    private String loginCommand(String username, String password) {
        return String.format("%s %s %s", ServerCommand.LOGIN.getCommandText(), username, password);
    }

    private String logoutCommand() {
        return ServerCommand.LOGOUT.getCommandText();
    }

    private String disconnectCommand() {
        return ServerCommand.DISCONNECT.getCommandText();
    }

    private String helpCommand() {
        return ServerCommand.HELP.getCommandText();
    }

    private String addPasswordWithCheck(String website, String usernameForSite, String password, String masterPassword) {
        return String.format("%s %s %s %s %s",
                             ServerCommand.ADD_PASSWORD_WITH_CHECK.getCommandText(), website, usernameForSite, password,
                             masterPassword);
    }

    private String removePassword(String website, String usernameForSite, String masterPassword) {
        return String.format("%s %s %s %s",
                             ServerCommand.REMOVE_PASSWORD.getCommandText(), website, usernameForSite, masterPassword);
    }

    private String retrieveCredentials(String website, String usernameForSite, String masterPassword) {
        return String.format("%s %s %s %s",
                             ServerCommand.RETRIEVE_CREDENTIAL.getCommandText(), website, usernameForSite,
                             masterPassword);
    }

    private String generatePassword(String website, String usernameForSite, int safePasswordLength,
                                    String masterPassword) {
        return String.format("%s %s %s %s %s",
                             ServerCommand.GENERATE_PASSWORD.getCommandText(), website,
                             usernameForSite, safePasswordLength, masterPassword);
    }
}