package password.vault.server.exceptions.password;

public class UsernameNotHavingCredentialsException extends Exception {
    public UsernameNotHavingCredentialsException(String message) {
        super(message);
    }

    public UsernameNotHavingCredentialsException() {

    }
}
