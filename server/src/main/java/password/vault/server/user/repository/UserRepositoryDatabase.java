package password.vault.server.user.repository;

import password.vault.server.cryptography.PasswordHash;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.exceptions.HashException;
import password.vault.server.exceptions.user.repository.InvalidUsernameException;
import password.vault.server.exceptions.user.repository.RegisterException;
import password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotLoggedInException;
import password.vault.server.requests.RegistrationRequest;

public class UserRepositoryDatabase implements UserRepository {
    private final static String VALID_USERNAME_PATTERN = "[a-zA-Z0-9-_]{3,}";

    private final DatabaseConnector databaseConnector;

    public UserRepositoryDatabase(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public void registerUser(RegistrationRequest registrationRequest) throws UserAlreadyRegisteredException,
            HashException, DatabaseConnectorException,
            InvalidUsernameException, RegisterException {

        if (!registrationRequest.username().matches(VALID_USERNAME_PATTERN)) {
            throw new InvalidUsernameException();
        }

        if (databaseConnector.isUserRegistered(registrationRequest.username())) {
            throw new UserAlreadyRegisteredException("user with username %s is already registered"
                                                             .formatted(registrationRequest.username()));
        }

        PasswordHash passwordHash = new PasswordHash(registrationRequest.password());

        boolean insertUserSuccess = databaseConnector.insertUser(registrationRequest.username(),
                                                                 registrationRequest.email(),
                                                                 passwordHash.getPasswordBytes(),
                                                                 passwordHash.getSalt());
        if (!insertUserSuccess) {
            throw new RegisterException("unable to insert user data");
        }
    }

    @Override
    public void registerUser(String username, String password, String email) throws InvalidUsernameException,
            UserAlreadyRegisteredException, HashException, DatabaseConnectorException, RegisterException {
        registerUser(new RegistrationRequest(username, password, email));
    }

    @Override
    public void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException {

    }

    @Override
    public void logOutUser(String username) throws UserNotLoggedInException {

    }

    @Override
    public boolean isUsernameRegistered(String username) {
        return false;
    }

    @Override
    public boolean isUsernameLoggedIn(String username) {
        return false;
    }
}
