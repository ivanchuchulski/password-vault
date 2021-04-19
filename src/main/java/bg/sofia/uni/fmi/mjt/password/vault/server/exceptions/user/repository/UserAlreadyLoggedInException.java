package bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository;

public class UserAlreadyLoggedInException extends LoginException {
    public UserAlreadyLoggedInException(String message) {
        super(message);
    }
}
