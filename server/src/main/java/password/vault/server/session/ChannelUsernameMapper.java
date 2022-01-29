package password.vault.server.session;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ChannelUsernameMapper {
    private final Map<SocketChannel, String> usernameForChannel;

    public ChannelUsernameMapper() {
        usernameForChannel = new HashMap<>();
    }

    public void addUsernameForChannel(SocketChannel socketChannel, String username) {
        usernameForChannel.put(socketChannel, username);
    }

    public void removeUsernameForChannel(SocketChannel socketChannel) {
        usernameForChannel.remove(socketChannel);
    }

    public boolean isUsernameAddedForChannel(SocketChannel socketChannel) {
        return usernameForChannel.containsKey(socketChannel);
    }

    public String getUsernameForChannel(SocketChannel socketChannel) {
        String username = usernameForChannel.get(socketChannel);
        if (username != null) {
            return username;
        } else {
            return "";
        }
    }
}
