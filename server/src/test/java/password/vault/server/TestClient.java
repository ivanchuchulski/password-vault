package password.vault.server;

import com.google.gson.Gson;
import password.vault.api.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TestClient {
    private static final String SERVER_HOST = "localhost";

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Gson gson;

    public TestClient(int serverPort) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, serverPort));

            reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
            gson = new Gson();
        } catch (IOException ioException) {
            throw new RuntimeException("error : connecting to server", ioException);
        }
    }

    public void sendRequest(String request) throws IOException {
        if (socketChannel.isConnected()) {
            writer.println(request);
        }
    }

    public Response receiveResponse() throws IOException {
        String line = reader.readLine();
        return gson.fromJson(line, Response.class);
    }

    public void closeConnection() throws IOException {
        reader.close();
        writer.close();
        socketChannel.close();
    }
}
