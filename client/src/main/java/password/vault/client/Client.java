package password.vault.client;

import com.google.gson.Gson;
import password.vault.api.Response;
import password.vault.api.ServerCommand;
import password.vault.api.ServerResponses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    private static final ServerResponses DISCONNECTED_FROM_SERVER_REPLY = ServerResponses.DISCONNECTED;
    private static final String USER_QUIT_COMMAND = "quit";

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private final Gson gson;

    public Client(int serverPort) {
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

    public static void main(String[] args) {
        Client wishListClient = new Client(SERVER_PORT);
        System.out.println("connected to the server.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("enter message: ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase(USER_QUIT_COMMAND)) {
                    wishListClient.sendRequest(ServerCommand.DISCONNECT.getCommandText());
                    break;
                }

                System.out.println("sending message <" + message + "> to the server...");

                wishListClient.sendRequest(message);

                Response response = wishListClient.receiveResponse();

                System.out.println("server replied : " + response.message());

                if (response.serverResponse().equals(DISCONNECTED_FROM_SERVER_REPLY)) {
                    break;
                }

            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
