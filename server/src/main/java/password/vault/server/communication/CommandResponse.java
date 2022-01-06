package password.vault.server.communication;

public record CommandResponse(boolean toDisconnect, String response) {

}
