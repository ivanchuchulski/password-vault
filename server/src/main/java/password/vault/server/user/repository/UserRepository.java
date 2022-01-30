package password.vault.server.user.repository;

import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.exceptions.HashException;
import password.vault.server.exceptions.user.repository.InvalidUsernameException;
import password.vault.server.exceptions.user.repository.LoginException;
import password.vault.server.exceptions.user.repository.LogoutException;
import password.vault.server.exceptions.user.repository.RegisterException;
import password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotLoggedInException;

public interface UserRepository {
    void registerUser(String username, String password, String email) throws InvalidUsernameException,
            UserAlreadyRegisteredException, HashException, DatabaseConnectorException, RegisterException;

    void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException,
            HashException, LoginException;

    void logOutUser(String username) throws UserNotLoggedInException, LogoutException;

    boolean isUsernameRegistered(String username);

    boolean isUsernameLoggedIn(String username);
}
