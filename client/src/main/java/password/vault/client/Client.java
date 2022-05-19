package password.vault.client;

import com.google.gson.Gson;
import password.vault.api.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private final Gson gson;

    public Client(String host, int serverPort) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(host, serverPort));

            reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);

            gson = new Gson();
        } catch (IOException ioException) {
            throw new RuntimeException("error : connecting to server", ioException);
        }
    }

    public Client() {
        this(SERVER_HOST, SERVER_PORT);
    }

    public void sendRequest(String request) {
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

    public void printConnectionInfo() {
        Socket socket = socketChannel.socket();
        System.out.println("connected to the server at ");
        System.out.printf("created socket at %s:%d%n", socket.getLocalAddress(), socket.getLocalPort());
        System.out.printf("connected to socket at %s%n", socket.getRemoteSocketAddress());
    }
}
