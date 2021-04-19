package bg.sofia.uni.fmi.mjt.password.vault.server.user.repository;

import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.InvalidUsernameException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.PasswordsNotMatchingException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository.UserNotLoggedInException;

public interface UserRepository {
    void registerUser(String username, String password, String repeatedPassword)
            throws PasswordsNotMatchingException, InvalidUsernameException, UserAlreadyRegisteredException;

    void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException;

    void logOutUser(String username) throws UserNotLoggedInException;

    boolean isUsernameRegistered(String username);

    boolean isUsernameLoggedIn(String username);
}
