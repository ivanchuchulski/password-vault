package password.vault.server.exceptions.user.repository;

public class UserNotLoggedInException extends Exception {
    public UserNotLoggedInException(String message) {
        super(message);
    }
}
