package bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository;

public class UserNotLoggedInException extends LogoutException {
    public UserNotLoggedInException(String message) {
        super(message);
    }
}
