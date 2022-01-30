package password.vault.server.exceptions.user.repository;

public class UserAlreadyLoggedInException extends Exception {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }

    public UserAlreadyLoggedInException() {

    }
}
