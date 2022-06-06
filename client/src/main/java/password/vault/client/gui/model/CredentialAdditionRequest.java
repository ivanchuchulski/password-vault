package password.vault.client.gui.model;

import java.util.Objects;

public final class CredentialAdditionRequest {
    private final String website;
    private final String username;
    private final String password;
    private final String masterPassword;

    public CredentialAdditionRequest(String website, String username, String password, String masterPassword) {
        this.website = website;
        this.username = username;
        this.password = password;
        this.masterPassword = masterPassword;
    }

    public String website() {
        return website;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String masterPassword() {
        return masterPassword;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CredentialAdditionRequest) obj;
        return Objects.equals(this.website, that.website) &&
                Objects.equals(this.username, that.username) &&
                Objects.equals(this.password, that.password) &&
                Objects.equals(this.masterPassword, that.masterPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(website, username, password, masterPassword);
    }

    @Override
    public String toString() {
        return "CredentialAdditionRequest[" +
                "website=" + website + ", " +
                "username=" + username + ", " +
                "password=" + password + ", " +
                "masterPassword=" + masterPassword + ']';
    }

}
