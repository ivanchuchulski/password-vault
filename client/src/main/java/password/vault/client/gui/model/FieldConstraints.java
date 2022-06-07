package password.vault.client.gui.model;

public class FieldConstraints {

    public static final String VALID_USERNAME_PATTERN = "[a-zA-Z0-9-_]{3,20}";
    public static final String VALID_EMAIL_PATTERN = "^[A-Za-z0-9]{2,32}@[A-Za-z]{2,16}\\.[A-Za-z]{2,7}$";

    public static final String WEBSITE_PATTERN = "([a-zA-Z]{2,10}\\.)?[a-zA-Z0-9]{2,20}\\.[a-zA-Z]{2,10}";
}
