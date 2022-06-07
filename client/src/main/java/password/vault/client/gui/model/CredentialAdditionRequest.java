package password.vault.client.gui.model;

import password.vault.client.gui.controllers.YesNoCheck;

public record CredentialAdditionRequest(String website, String username, String password, YesNoCheck yesNoCheck) {


}
