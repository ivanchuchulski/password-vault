package password.vault.server.dto;

import java.util.Objects;

public final class CredentialDTO {
    private final String username;
    private final String website;
    private final String usernameForSite;
    private final String encryptedPassword;

    public CredentialDTO(String username, String website, String usernameForSite, String encryptedPassword) {
        this.username = username;
        this.website = website;
        this.usernameForSite = usernameForSite;
        this.encryptedPassword = encryptedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getWebsite() {
        return website;
    }

    public String getUsernameForSite() {
        return usernameForSite;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CredentialDTO that = (CredentialDTO) o;
        return Objects.equals(username, that.username) && Objects
                .equals(website, that.website) && Objects
                .equals(usernameForSite, that.usernameForSite) && Objects
                .equals(encryptedPassword, that.encryptedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, website, usernameForSite, encryptedPassword);
    }

    @Override
    public String toString() {
        return "CredentialDTO{" +
                "username='" + username + '\'' +
                ", website='" + website + '\'' +
                ", usernameForSite='" + usernameForSite + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                '}';
    }
}
