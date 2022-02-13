package password.vault.server.user.repository.in.memory;

import com.google.gson.Gson;
import password.vault.server.cryptography.PasswordHash;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.user.repository.User;
import password.vault.server.user.repository.UserRepository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class UserRepositoryInMemory implements UserRepository {
    private final static String VALID_USERNAME_PATTERN = "[a-zA-Z0-9-_]{3,}";

    private static final String MASTER_PASSWORD_FOR_TESTING = "4567";

    private final Set<User> registeredUsers;
    private final Set<String> loggedInUsernames;
    private final Path registeredUsersFile;
    private static final Gson gson = new Gson();

    public UserRepositoryInMemory(Path registeredUsersFile) {
        registeredUsers = new HashSet<>();
        loggedInUsernames = new HashSet<>();
        this.registeredUsersFile = registeredUsersFile;

        createUsersFile();
        readUsersFromFile();
    }

    @Override
    public void registerUser(String username, String password, String email, String masterPassword)
            throws InvalidUsernameException, UserAlreadyRegisteredException, PasswordHasher.HashException {
        User user = new User(username, hashPassword(password));

        if (!isUsernameCorrect(user.username())) {
            throw new InvalidUsernameException(String.format("username %s is invalid, select a valid one", username));
        }

        if (isUserRegistered(user)) {
            throw new UserAlreadyRegisteredException(
                    String.format("username %s is already taken, select another one", username));
        }

        registeredUsers.add(user);
        writeUserToFile(user);
    }

    @Override
    public void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException,
            PasswordHasher.HashException {
        User user = new User(username, hashPassword(password));

        if (!isUserRegistered(user)) {
            throw new UserNotFoundException("invalid username/password combination");
        }

        if (isUsernameLoggedIn(username)) {
            throw new UserAlreadyLoggedInException("can't login user again, user is already logged in");
        }

        loggedInUsernames.add(username);
    }

    @Override
    public void logOutUser(String username) throws UserNotLoggedInException {
        if (!isUsernameLoggedIn(username)) {
            throw new UserNotLoggedInException("user is not logged in");
        }

        loggedInUsernames.remove(username);
    }

    @Override
    public boolean isUsernameLoggedIn(String username) {
        return loggedInUsernames.contains(username);
    }

    @Override
    public PasswordHash getMasterPassword(String username) {
        try {
            return new PasswordHash(MASTER_PASSWORD_FOR_TESTING);
        } catch (PasswordHasher.HashException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isUsernameRegistered(String username) {
        return registeredUsers.stream()
                              .map(User::username)
                              .anyMatch(username::equals);
    }

    private boolean isUsernameCorrect(String username) {
        return username.matches(VALID_USERNAME_PATTERN);
    }

    private boolean isUserRegistered(User user) {
        return registeredUsers.contains(user);
    }

    private String hashPassword(String password) throws PasswordHasher.HashException {
        return PasswordHasher.computeHash(password, PasswordHasher.SHA256_MESSAGE_DIGEST_INSTANCE);
    }

    private void createUsersFile() {
        try {
            Files.createFile(registeredUsersFile);
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
            System.out.println("users file already exists!");
        } catch (IOException ioException) {
            throw new RuntimeException("error : creating users file", ioException);
        }
    }

    private void writeUserToFile(User user) {
        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(new FileWriter(String.valueOf(registeredUsersFile), true))) {

            String userAsJSON = gson.toJson(user);
            bufferedWriter.write(userAsJSON);
            bufferedWriter.write(System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException ioException) {
            throw new RuntimeException("error : writing users to file", ioException);
        }
    }

    private void readUsersFromFile() {
        try (BufferedReader bufferedReader =
                     new BufferedReader(new FileReader(String.valueOf(registeredUsersFile)))) {

            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                User user = gson.fromJson(line, User.class);
                registeredUsers.add(user);
            }

        } catch (IOException ioException) {
            throw new RuntimeException("error : writing users to file", ioException);
        }
    }
}
