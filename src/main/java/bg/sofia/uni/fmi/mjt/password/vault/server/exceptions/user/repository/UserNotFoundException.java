package bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository;

public class UserNotFoundException extends LoginException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
