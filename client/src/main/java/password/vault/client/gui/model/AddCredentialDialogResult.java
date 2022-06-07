package password.vault.client.gui.model;

import password.vault.client.gui.controllers.YesNoCheck;

public record AddCredentialDialogResult(String website, String username, String password, YesNoCheck yesNoCheck) {


}
