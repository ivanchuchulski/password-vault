package password.vault.server.user.repository;

import password.vault.server.exceptions.user.repository.InvalidUsernameException;
import password.vault.server.exceptions.user.repository.PasswordsNotMatchingException;
import password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotLoggedInException;

public interface UserRepository {
    void registerUser(String username, String password, String repeatedPassword)
            throws PasswordsNotMatchingException, InvalidUsernameException, UserAlreadyRegisteredException;

    void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException;

    void logOutUser(String username) throws UserNotLoggedInException;

    boolean isUsernameRegistered(String username);

    boolean isUsernameLoggedIn(String username);
}
