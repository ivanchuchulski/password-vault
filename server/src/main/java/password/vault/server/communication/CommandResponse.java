package password.vault.server.communication;


import password.vault.api.Response;

public record CommandResponse(boolean toDisconnect, Response response) {

}
