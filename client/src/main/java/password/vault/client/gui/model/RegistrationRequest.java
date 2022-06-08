package password.vault.client.gui.model;

import java.util.Objects;

public final class RegistrationRequest {

    private final String username;
    private final String email;
    private final String password;
    private final String passwordRepeated;
    private final String masterPassword;
    private final String masterPasswordRepeated;

    public RegistrationRequest(String username, String email, String password,
                               String passwordRepeated, String masterPassword, String masterPasswordRepeated) throws
            RegistrationRequestException {

        if (username.isBlank() || email.isBlank()
                || password.isBlank() || passwordRepeated.isBlank()
                || masterPassword.isBlank() || masterPasswordRepeated.isBlank()) {
            throw new RegistrationRequestException("all fields are necessary!");
        }

        if (!username.matches(FieldConstraints.VALID_USERNAME_PATTERN)) {
            throw new RegistrationRequestException("username is not valid!");
        }

        if (!email.matches(FieldConstraints.VALID_EMAIL_PATTERN)) {
            throw new RegistrationRequestException("error : email is not valid!");
        }

        if (!password.equals(passwordRepeated)) {
            throw new RegistrationRequestException("passwords should match!");
        }

        if (!masterPassword.equals(masterPasswordRepeated)) {
            throw new RegistrationRequestException("master passwords should match!");
        }

        this.username = username;
        this.email = email;
        this.password = password;
        this.passwordRepeated = passwordRepeated;
        this.masterPassword = masterPassword;
        this.masterPasswordRepeated = masterPasswordRepeated;
    }

    public String username() {
        return username;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String passwordRepeated() {
        return passwordRepeated;
    }

    public String masterPassword() {
        return masterPassword;
    }

    public String masterPasswordRepeated() {
        return masterPasswordRepeated;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (RegistrationRequest) obj;
        return Objects.equals(this.username, that.username) &&
                Objects.equals(this.email, that.email) &&
                Objects.equals(this.password, that.password) &&
                Objects.equals(this.passwordRepeated, that.passwordRepeated) &&
                Objects.equals(this.masterPassword, that.masterPassword) &&
                Objects.equals(this.masterPasswordRepeated, that.masterPasswordRepeated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, password, passwordRepeated, masterPassword, masterPasswordRepeated);
    }

    @Override
    public String toString() {
        return "RegistrationRequest[" +
                "username=" + username + ", " +
                "email=" + email + ", " +
                "password=" + password + ", " +
                "passwordRepeated=" + passwordRepeated + ", " +
                "masterPassword=" + masterPassword + ", " +
                "masterPasswordRepeated=" + masterPasswordRepeated + ']';
    }

    public class RegistrationRequestException extends Exception {
        public RegistrationRequestException(String message) {
            super(message);
        }
    }
}
