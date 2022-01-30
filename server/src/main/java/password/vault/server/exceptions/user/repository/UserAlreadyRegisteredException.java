package password.vault.server.exceptions.user.repository;

public class UserAlreadyRegisteredException extends Exception {
    public UserAlreadyRegisteredException(String message) {
        super(message);
    }
}
