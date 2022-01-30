package password.vault.api;

import java.util.Objects;

public final class Response {
    private final ServerResponses serverResponse;
    private final String message;

    public Response(ServerResponses serverResponse, String message) {
        this.serverResponse = serverResponse;
        this.message = message;
    }

    public ServerResponses serverResponse() {
        return serverResponse;
    }

    public String message() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Response) obj;
        return Objects.equals(this.serverResponse, that.serverResponse) &&
                Objects.equals(this.message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverResponse, message);
    }

    @Override
    public String toString() {
        return "Response[" +
                "serverResponse=" + serverResponse + ", " +
                "message=" + message + ']';
    }

}
