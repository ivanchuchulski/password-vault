package password.vault.server.user.repository;

import password.vault.server.exceptions.user.repository.InvalidUsernameException;
import password.vault.server.exceptions.user.repository.PasswordsNotMatchingException;
import password.vault.server.exceptions.user.repository.UserAlreadyLoggedInException;
import password.vault.server.exceptions.user.repository.UserAlreadyRegisteredException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotLoggedInException;

public class UserRepositoryDatabase implements UserRepository {

    @Override
    public void registerUser(String username, String password, String repeatedPassword) throws
            PasswordsNotMatchingException, InvalidUsernameException, UserAlreadyRegisteredException {

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
