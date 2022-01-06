package password.vault.server.exceptions.user.repository;

public class UserAlreadyRegisteredException extends RegisterException {
    public UserAlreadyRegisteredException(String message) {
        super(message);
    }
}
