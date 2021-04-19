package bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.user.repository;

public class PasswordsNotMatchingException extends RegisterException {
    public PasswordsNotMatchingException(String message) {
        super(message);
    }
}
