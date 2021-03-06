package bg.sofia.uni.fmi.mjt.password.vault.server.user.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.user.User;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.password.PasswordEncryptorException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.InvalidUsernameException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.LoginException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.LogoutException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.PasswordsNotMatchingException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.RegisterException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserNotLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.server.password.encryptor.PasswordEncryptor;
import com.google.gson.Gson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserRepositoryTest {
    private static final String SYSTEM_FILE_SEPARATOR = File.separator;

    private static UserRepository userRepository;
    private static final Path REGISTERED_USERS_FILE =
            Path.of("resources" + SYSTEM_FILE_SEPARATOR + "repository-test-users.txt");

    private static final String SAMPLE_USERNAMES_FILEPATH = "resources" + File.separator + "unique-usernames.txt";
    private static List<String> usernamesForTesting;
    private static final Random random = new Random();

    private static final Gson gson = new Gson();

    private static final String INITIALLY_ADDED_TEST_USERNAME = "test_test";
    private static final String PASSWORD_FOR_TESTING = "1234";

    @BeforeClass
    public static void beforeClass() throws Exception {
        Files.deleteIfExists(REGISTERED_USERS_FILE);
        Files.createFile(REGISTERED_USERS_FILE);

        addOneUserToUsersFile();

        usernamesForTesting = getUniqueNamesFromFile();

        userRepository = new UserRepositoryInMemory(REGISTERED_USERS_FILE);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        Files.deleteIfExists(REGISTERED_USERS_FILE);
    }

    @Test
    public void testTheUserFromTheFileIsRegistered() {
        assertTrue(userRepository.isUsernameRegistered(INITIALLY_ADDED_TEST_USERNAME));
    }

    @Test
    public void testRegisteringNonRegisteredUsernameRegistersIt() throws RegisterException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password);

        assertTrue(userRepository.isUsernameRegistered(username));
    }

    @Test(expected = PasswordsNotMatchingException.class)
    public void testRegisteringWithWrongRepeatedPassword() throws RegisterException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password + "1234");
    }

    @Test(expected = InvalidUsernameException.class)
    public void testRegisteringWithInvalidUsername() throws RegisterException {
        String username = "@###";
        String password = PASSWORD_FOR_TESTING;
        userRepository.registerUser(username, password, password);
    }

    @Test(expected = UserAlreadyRegisteredException.class)
    public void testRegisteringAnAlreadyRegisteredUser() throws RegisterException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password);
        userRepository.registerUser(username, password, password);
    }

    @Test(expected = UserNotFoundException.class)
    public void testLoginNonRegisteredUser() throws LoginException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.logInUser(username, password);
    }

    @Test(expected = UserAlreadyLoggedInException.class)
    public void testLoginAlreadyLoggedInUser() throws LoginException, RegisterException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password);
        userRepository.logInUser(username, password);
        userRepository.logInUser(username, password);
    }

    @Test
    public void testLoginARegisteredUserLogsHimIn() throws RegisterException, LoginException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password);
        userRepository.logInUser(username, password);

        assertTrue(userRepository.isUsernameLoggedIn(username));
    }

    @Test(expected = UserNotLoggedInException.class)
    public void testLogoutANonLoggedInUser() throws LogoutException {
        String username = getUniqueUsername();

        userRepository.logOutUser(username);
    }

    @Test
    public void testLogOutALoggedInUser() throws RegisterException, LoginException, LogoutException {
        String username = getUniqueUsername();
        String password = PASSWORD_FOR_TESTING;

        userRepository.registerUser(username, password, password);
        userRepository.logInUser(username, password);
        userRepository.logOutUser(username);

        assertFalse(userRepository.isUsernameLoggedIn(username));
    }

    private static void addOneUserToUsersFile() throws PasswordEncryptorException {
        SecretKey key = PasswordEncryptor.getKeyFromString(PASSWORD_FOR_TESTING);

        User testUser = new User(INITIALLY_ADDED_TEST_USERNAME,
                                 PasswordEncryptor.encrypt(PASSWORD_FOR_TESTING, key));

        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(new FileWriter(String.valueOf(REGISTERED_USERS_FILE), true))) {

            String userAsJSON = gson.toJson(testUser);
            bufferedWriter.write(userAsJSON);
            bufferedWriter.write(System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException ioException) {
            throw new RuntimeException("error : writing users to file", ioException);
        }
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
}