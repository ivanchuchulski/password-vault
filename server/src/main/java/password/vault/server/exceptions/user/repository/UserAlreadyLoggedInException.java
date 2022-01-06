package password.vault.server.exceptions.user.repository;

public class UserAlreadyLoggedInException extends LoginException {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }
}
