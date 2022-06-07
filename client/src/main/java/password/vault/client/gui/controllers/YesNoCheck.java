package password.vault.client.gui.controllers;

public enum YesNoCheck {
    YES("Yes"),
    NO("No");

    private final String text;

    YesNoCheck(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}