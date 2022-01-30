package password.vault.server.exceptions.user.repository;

public class PasswordsNotMatchingException extends Exception {
    public PasswordsNotMatchingException(String message) {
        super(message);
    }
}
