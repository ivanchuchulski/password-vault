package password.vault.server.user.repository;

import password.vault.server.cryptography.PasswordHash;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.requests.RegistrationRequest;

public class UserRepositoryDatabase implements UserRepository {
    private final static String VALID_USERNAME_PATTERN = "[a-zA-Z0-9-_]{3,}";

    private final DatabaseConnector databaseConnector;

    public UserRepositoryDatabase(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public void registerUser(RegistrationRequest registrationRequest) throws UserAlreadyRegisteredException,
            PasswordHasher.HashException, DatabaseConnectorException,
            InvalidUsernameException, RegisterException {
        if (!registrationRequest.username().matches(VALID_USERNAME_PATTERN)) {
            throw new InvalidUsernameException();
        }

        if (databaseConnector.isUserRegistered(registrationRequest.username())) {
            throw new UserAlreadyRegisteredException("user with username %s is already registered"
                                                             .formatted(registrationRequest.username()));
        }

        PasswordHash passwordHash = new PasswordHash(registrationRequest.password());
        PasswordHash masterPasswordHash = new PasswordHash(registrationRequest.masterPassword());

        boolean insertUserSuccess = databaseConnector.insertUser(registrationRequest.username(),
                                                                 registrationRequest.email(),
                                                                 passwordHash, masterPasswordHash);
        if (!insertUserSuccess) {
            throw new RegisterException("unable to insert user data");
        }
    }

    @Override
    public void registerUser(String username, String password, String email, String masterPassword) throws
            InvalidUsernameException,
            UserAlreadyRegisteredException, PasswordHasher.HashException, DatabaseConnectorException,
            RegisterException {
        registerUser(new RegistrationRequest(username, email, password, masterPassword));
    }

    @Override
    public void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException,
            LoginException, PasswordHasher.HashException {
        try {
            if (!databaseConnector.isUserRegistered(username)) {
                throw new UserNotFoundException("user with username %s is not registered".formatted(username));
            }

            PasswordHash retrievedHash = databaseConnector.getPasswordForUser(username);
            byte[] salt = retrievedHash.getSalt();
            PasswordHash computedHash = new PasswordHash(password, salt);

            if (!computedHash.equals(retrievedHash)) {
                throw new UserNotFoundException();
            }

            if (databaseConnector.isUserLoggedIn(username)) {
                throw new UserAlreadyLoggedInException();
            }

            if (!databaseConnector.loginUser(username)) {
                throw new LoginException("unable to login user");
            }
        } catch (DatabaseConnectorException e) {
            throw new LoginException("database connection failed on logging in user");
        }
    }

    @Override
    public void logOutUser(String username) throws UserNotLoggedInException, LogoutException {
        try {
            if (!databaseConnector.isUserLoggedIn(username)) {
                throw new UserNotLoggedInException();
            }

            if (!databaseConnector.logoutUser(username)) {
                throw new LogoutException("unable to logout user");
            }
        } catch (DatabaseConnectorException e) {
            throw new LogoutException("database connection failed to logout user");
        }
    }

    @Override
    public boolean isUsernameRegistered(String username) {
        try {
            return databaseConnector.isUserRegistered(username);
        } catch (DatabaseConnectorException e) {
            return false;
        }
    }

    @Override
    public boolean isUsernameLoggedIn(String username) {
        try {
            return databaseConnector.isUserLoggedIn(username);
        } catch (DatabaseConnectorException e) {
            return false;
        }
    }

    @Override
    public PasswordHash getMasterPassword(String username) throws DatabaseConnectorException {
        return databaseConnector.getMasterPassword(username);
    }
}
