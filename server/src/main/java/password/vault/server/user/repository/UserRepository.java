package password.vault.server.user.repository;

import password.vault.server.cryptography.PasswordHash;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.db.DatabaseConnectorException;

public interface UserRepository {
    void registerUser(String username, String password, String email, String masterPassword) throws InvalidUsernameException,
            UserAlreadyRegisteredException, PasswordHasher.HashException, DatabaseConnectorException, RegisterException;

    void logInUser(String username, String password) throws UserNotFoundException, UserAlreadyLoggedInException,
            PasswordHasher.HashException, LoginException;

    void logOutUser(String username) throws UserNotLoggedInException, LogoutException;

    boolean isUsernameRegistered(String username);

    boolean isUsernameLoggedIn(String username);

    PasswordHash getMasterPassword(String username) throws DatabaseConnectorException;

    class InvalidUsernameException extends Exception {
        public InvalidUsernameException(String message) {
            super(message);
        }

        public InvalidUsernameException() {
        }
    }

    class LoginException extends Exception {
        public LoginException(String message) {
            super(message);
        }
    }

    class LogoutException extends Exception {
        public LogoutException(String message) {
            super(message);
        }
    }

    class PasswordsNotMatchingException extends Exception {
        public PasswordsNotMatchingException(String message) {
            super(message);
        }
    }

    class RegisterException extends Exception {
        public RegisterException(String message) {
            super(message);
        }
    }

    class UserAlreadyLoggedInException extends Exception {
        public UserAlreadyLoggedInException(String message) {
            super(message);
        }

        public UserAlreadyLoggedInException() {

        }
    }

    class UserAlreadyRegisteredException extends Exception {
        public UserAlreadyRegisteredException(String message) {
            super(message);
        }
    }

    class UserNotFoundException extends Exception {
        public UserNotFoundException(String message) {
            super(message);
        }

        public UserNotFoundException() {
            super();
        }
    }

    class UserNotLoggedInException extends Exception {
        public UserNotLoggedInException(String message) {
            super(message);
        }

        public UserNotLoggedInException() {

        }
    }
}
