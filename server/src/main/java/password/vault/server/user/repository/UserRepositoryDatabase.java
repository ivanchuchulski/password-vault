package password.vault.server.user.repository;

import password.vault.server.cryptography.PasswordHash;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.exceptions.HashException;
import password.vault.server.exceptions.user.repository.InvalidUsernameException;
import password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotLoggedInException;
import password.vault.server.requests.RegistrationRequest;

public class UserRepositoryDatabase {
    private final static String VALID_USERNAME_PATTERN = "[a-zA-Z0-9-_]{3,}";

    private final DatabaseConnector databaseConnector;

    public UserRepositoryDatabase(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public void registerUser(RegistrationRequest registrationRequest) throws
            InvalidUsernameException, UserAlreadyRegisteredException {
        try {

            if (databaseConnector.isUserRegistered(registrationRequest.username())) {
                throw new UserAlreadyRegisteredException("user with username %s is already registered"
                                                                 .formatted(registrationRequest.username()));
            }

            if (!registrationRequest.username().matches(VALID_USERNAME_PATTERN)) {
                throw new InvalidUsernameException();
            }

            PasswordHash passwordHash = new PasswordHash(registrationRequest.password());
            databaseConnector.insertUser(registrationRequest.username(), registrationRequest.email(),
                                         passwordHash.getPasswordBytes(), passwordHash.getSalt());
        } catch (HashException e) {
            e.printStackTrace();
        }
    }

    public void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException {

    }

    public void logOutUser(String username) throws UserNotLoggedInException {

    }

    public boolean isUsernameRegistered(String username) {
        return false;
    }

    public boolean isUsernameLoggedIn(String username) {
        return false;
    }
}
