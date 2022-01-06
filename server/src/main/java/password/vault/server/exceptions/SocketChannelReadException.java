package password.vault.server.exceptions;

public class SocketChannelReadException extends Exception {
    public SocketChannelReadException(String message) {
        super(message);
    }

    public SocketChannelReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
