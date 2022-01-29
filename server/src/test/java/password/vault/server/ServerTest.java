package password.vault.server;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import password.vault.api.ServerCommand;
import password.vault.api.ServerResponses;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.exceptions.password.PasswordGeneratorException;
import password.vault.server.exceptions.password.PasswordSafetyCheckerException;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServerTest {
    private static final int SERVER_PORT = 7778;
    private static final String SYSTEM_FILE_SEPARATOR = File.separator;

    private static Thread serverRunnerThread;
    private static Server server;
    private static TestClient client;

    private static final String SAMPLE_USERNAMES_FILEPATH =
            "test_resources" + SYSTEM_FILE_SEPARATOR + "unique-usernames.txt";
    private static List<String> usernamesForTesting;
    private static Random random;
    private static final String PASSWORD_FOR_TESTING = "1234";
    private static final String WEBSITE_FOR_TESTING = "facebook.com";

    private static final Path REGISTERED_USERS_FILE =
            Path.of("resources" + SYSTEM_FILE_SEPARATOR + "server-test-users.txt");
    private static final Path CREDENTIALS_FILE =
            Path.of("resources" + SYSTEM_FILE_SEPARATOR + "server-test-credentials.txt");

    private static final int NUMBER_OF_GENERATED_SAFE_PASSWORDS = 1;


    private static final String SAMPLE_SAFE_PASSWORD = "_rrR~S>k$[8+Ps/x2WyaFv";
    private static final int SAFE_PASSWORD_LENGTH = SAMPLE_SAFE_PASSWORD.length();

    private static PasswordSafetyChecker passwordSafetyChecker;

    private static PasswordGenerator passwordGenerator;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException, PasswordGeneratorException,
            PasswordSafetyCheckerException {

        PasswordGeneratorResponse passwordGeneratorResponse =
                new PasswordGeneratorResponse(true,
                                              NUMBER_OF_GENERATED_SAFE_PASSWORDS,
                                              new String[]{SAMPLE_SAFE_PASSWORD});

        PasswordSafetyResponse passwordSafetyResponse = new PasswordSafetyResponse(false, 0);

        passwordGenerator = Mockito.mock(PasswordGenerator.class);
        passwordSafetyChecker = Mockito.mock(PasswordSafetyChecker.class);

        when(passwordGenerator.generateSafePassword(anyInt())).thenReturn(passwordGeneratorResponse);
        when(passwordSafetyChecker.checkPassword(anyString())).thenReturn(passwordSafetyResponse);

        Files.deleteIfExists(REGISTERED_USERS_FILE);
        Files.deleteIfExists(CREDENTIALS_FILE);

        random = new Random();
        usernamesForTesting = getUniqueNamesFromFile();

        serverRunnerThread = new Thread(() -> {
            server = new Server(SERVER_PORT, REGISTERED_USERS_FILE, CREDENTIALS_FILE, passwordSafetyChecker,
                                passwordGenerator);
            server.start();
        });

        serverRunnerThread.start();
    }

    @AfterClass
    public static void tearDownAfterClass() throws IOException {
        serverRunnerThread.interrupt();
        server.stop();

        Files.deleteIfExists(REGISTERED_USERS_FILE);
        Files.deleteIfExists(CREDENTIALS_FILE);
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

        String actualResponse = sendRequestAndGetResponse(unknownCommandText);

        String expectedResponse = ServerResponses.UNKNOWN_COMMAND.getResponseText();

        assertEquals("when sending unknown command an unknown command response should be received",
                     expectedResponse,
                     actualResponse);
    }

    @Test
    public void testServerResponseWhenRequestIsDisconnect() throws IOException {
        String actualResponse = sendRequestAndGetResponse(disconnectCommand());

        String expectedResponse = ServerResponses.DISCONNECTED.getResponseText();

        assertEquals("when sending disconnect command a disconnect response should be received",
                     expectedResponse,
                     actualResponse);
    }

    @Test
    public void testServerResponseToHelpCommand() throws IOException {
        String actualResponse = sendRequestAndGetResponse(helpCommand());

        String expectedResponse = ServerCommand.printHelp();
        assertEquals("when sending help command an overview of all commands should be received",
                     expectedResponse,
                     actualResponse);
    }

    @Test
    public void testServerResponseWhenCommandHasIncorrectNumberOfArguments() throws IOException {
        String command = disconnectCommand() + " me";
        String actualResponse = sendRequestAndGetResponse(command);

        String expectedResponse = ServerResponses.WRONG_COMMAND_NUMBER_OF_ARGUMENTS
                .getResponseText().formatted(ServerCommand.DISCONNECT.getCommandOverview());

        assertEquals("when sending disconnect command a disconnect response should be received",
                     expectedResponse,
                     actualResponse);
    }

    @Test
    public void testValidRegistration() throws IOException {
        String username = getUniqueUsername();

        String response = sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING,
                                                                    PASSWORD_FOR_TESTING));

        String expectedResponse = ServerResponses.REGISTRATION_SUCCESS.getResponseText().formatted(username);

        assertEquals("valid registration should return a success response", expectedResponse, response);
    }

    @Test
    public void testValidLogin() throws IOException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        String response = sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        String expectedResponse = ServerResponses.LOGIN_SUCCESS.getResponseText().formatted(username);

        assertEquals("valid login should return a success response", expectedResponse, response);
    }

    @Test
    public void testValidLogout() throws IOException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));
        String response = sendRequestAndGetResponse(logoutCommand());

        String expectedResponse = ServerResponses.LOGOUT_SUCCESS.getResponseText().formatted(username);

        assertEquals("valid logout should return a success response", expectedResponse, response);
    }

    @Test
    public void testLoggingInWhenAlreadyLoggedIn() throws IOException {
        String username = getUniqueUsername();

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        String response = sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));

        String expectedResponse = ServerResponses.LOGIN_ERROR
                .getResponseText().formatted("can't login user again, user is already logged in");

        assertEquals("logging in when user is already logged in should return a error response", expectedResponse,
                     response);
    }

    @Test
    public void testLogoutWithoutLoggingIn() throws IOException {
        String response = sendRequestAndGetResponse(logoutCommand());

        String expectedResponse = ServerResponses.LOGOUT_ERROR.getResponseText().formatted("user is not logged in");

        assertEquals("logging out when a user has not logged in should return a error response", expectedResponse,
                     response);
    }

    @Test
    public void testRegisteringWithWrongRepeatedPassword() throws IOException {
        String username = getUniqueUsername();

        String response = sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING,
                                                                    PASSWORD_FOR_TESTING + "1234"));

        String expectedResponse = ServerResponses.
                REGISTRATION_ERROR.getResponseText().formatted("passwords do not match");

        assertEquals("when trying to register with non matching passwords and error message should be returned",
                     expectedResponse,
                     response);
    }

    @Test
    public void testAddingAPasswordAndRetrievingIt() throws IOException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(addPassword(WEBSITE_FOR_TESTING, usernameForSite, PASSWORD_FOR_TESTING));
        String response = sendRequestAndGetResponse(retrieveCredentials(WEBSITE_FOR_TESTING, usernameForSite));
        sendRequestAndGetResponse(logoutCommand());

        String expectedResponse =
                ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS.getResponseText().formatted(PASSWORD_FOR_TESTING);

        assertEquals("when adding a password and retrieving it the returned password should be the provided one",
                     expectedResponse,
                     response);
    }

    @Test
    public void testRemovingAPasswordRemovesIt() throws IOException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(addPassword(WEBSITE_FOR_TESTING, usernameForSite, PASSWORD_FOR_TESTING));
        String response = sendRequestAndGetResponse(removePassword(WEBSITE_FOR_TESTING, usernameForSite));
        sendRequestAndGetResponse(logoutCommand());

        String expectedResponse = ServerResponses.CREDENTIAL_REMOVAL_SUCCESS.getResponseText()
                                                                            .formatted(WEBSITE_FOR_TESTING,
                                                                                       usernameForSite);
        assertEquals("when removing a previously added password the response should be successful removal",
                     expectedResponse, response);
    }

    @Test
    public void testGeneratingAPasswordReturnsTheGeneratedOne() throws IOException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        sendRequestAndGetResponse(registerCommand(username, PASSWORD_FOR_TESTING, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(loginCommand(username, PASSWORD_FOR_TESTING));
        sendRequestAndGetResponse(generatePassword(WEBSITE_FOR_TESTING, usernameForSite, SAFE_PASSWORD_LENGTH));
        String response = sendRequestAndGetResponse(retrieveCredentials(WEBSITE_FOR_TESTING, usernameForSite));
        sendRequestAndGetResponse(disconnectCommand());

        String expectedResponse = ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS.getResponseText()
                                                                              .formatted(SAMPLE_SAFE_PASSWORD);
        assertEquals("generated password should be the sample mocked password",
                     expectedResponse, response);
    }

    @Test
    public void testAddingInPasswordFromNonLoggedInUser() throws IOException {
        String username = getUniqueUsername();
        String usernameForSite = buildUsernameForSiteFromUsername(username);

        String response = sendRequestAndGetResponse(addPassword(WEBSITE_FOR_TESTING, usernameForSite,
                                                                PASSWORD_FOR_TESTING));

        String expectedResponse = ServerResponses.NOT_LOGGED_IN.getResponseText();

        assertEquals("when trying to add a password for non-logged user an error response is expected",
                     expectedResponse, response);
    }

    private String sendRequestAndGetResponse(String request) throws IOException {
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

    private String registerCommand(String username, String password, String repeatedPassword) {
        return String.format("%s %s %s %s",
                             ServerCommand.REGISTER.getCommandText(), username, password, repeatedPassword);
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

    private String addPassword(String website, String usernameForSite, String password) {
        return String
                .format("%s %s %s %s", ServerCommand.ADD_PASSWORD.getCommandText(), website, usernameForSite, password);
    }

    private String removePassword(String website, String usernameForSite) {
        return String.format("%s %s %s", ServerCommand.REMOVE_PASSWORD.getCommandText(), website, usernameForSite);
    }

    private String retrieveCredentials(String website, String usernameForSite) {
        return String
                .format("%s %s %s", ServerCommand.RETRIEVE_CREDENTIALS.getCommandText(), website, usernameForSite);
    }

    private String generatePassword(String website, String usernameForSite, int safePasswordLength) {
        return String.format("%s %s %s %s", ServerCommand.GENERATE_PASSWORD.getCommandText(), website,
                             usernameForSite, safePasswordLength);
    }
}