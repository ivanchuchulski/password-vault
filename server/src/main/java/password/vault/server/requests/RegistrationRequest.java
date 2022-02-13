package password.vault.server.requests;

public record RegistrationRequest(String username, String email, String password, String masterPassword) {

}
