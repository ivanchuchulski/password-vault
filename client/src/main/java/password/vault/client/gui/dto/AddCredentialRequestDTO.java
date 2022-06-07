package password.vault.client.gui.dto;

public record AddCredentialRequestDTO(String website, String username, String password, String masterPassword) {

}
