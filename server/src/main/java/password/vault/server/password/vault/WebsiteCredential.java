package password.vault.server.password.vault;

import java.util.Objects;

public final class WebsiteCredential {
    private static final String WEBSITE_PATTERN = "([a-zA-Z]{2,10}\\.)?[a-zA-Z0-9]{2,20}\\.[a-zA-Z]{2,10}";
    private static final String USERNAME_FOR_SITE_PATTERN = "[a-zA-Z0-9-_]{3,20}";

    private final String website;
    private final String usernameForSite;
    private final String password;

    public WebsiteCredential(String website, String usernameForSite, String password) throws InvalidWebsiteException,
            InvalidUsernameForSiteException {
        if (!isWebsiteValid(website)) {
            throw new InvalidWebsiteException(String.format("website %s is invalid", website));
        }

        if (!isUsernameForSiteValid(usernameForSite)) {
            throw new InvalidUsernameForSiteException(String.format("username for site %s is invalid",
                                                                    usernameForSite));
        }

        this.website = website;
        this.usernameForSite = usernameForSite;
        this.password = password;
    }

    CredentialIdentifier getCredentialIdentifier() {
        return new CredentialIdentifier(website, usernameForSite);
    }

    public String website() {
        return website;
    }

    public String usernameForSite() {
        return usernameForSite;
    }

    public String password() {
        return password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WebsiteCredential) obj;
        return Objects.equals(this.website, that.website) &&
                Objects.equals(this.usernameForSite, that.usernameForSite) &&
                Objects.equals(this.password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(website, usernameForSite, password);
    }

    @Override
    public String toString() {
        return "WebsiteCredential[" +
                "website=" + website + ", " +
                "usernameForSite=" + usernameForSite + ", " +
                "password=" + password + ']';
    }

    private boolean isWebsiteValid(String website) {
        return website.matches(WEBSITE_PATTERN);
    }

    private boolean isUsernameForSiteValid(String usernameForSite) {
        return usernameForSite.matches(USERNAME_FOR_SITE_PATTERN);
    }

    public static class InvalidUsernameForSiteException extends Exception{
        public InvalidUsernameForSiteException(String message) {
            super(message);
        }
    }

    public static class InvalidWebsiteException extends Exception {
        public InvalidWebsiteException(String message) {
            super(message);
        }
    }
}
