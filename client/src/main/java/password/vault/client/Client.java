package password.vault.client;

import com.google.gson.Gson;
import password.vault.api.Response;
import password.vault.api.ServerResponses;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    private static final ServerResponses DISCONNECTED_FROM_SERVER_REPLY = ServerResponses.DISCONNECTED;

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

    public void sendRequest(String request)  {
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
        System.out.printf("created socket at %s:%d%n", socket.getLocalAddress(), socket.getLocalPort());
        System.out.printf("connected to socket at %s:%d%n", socket.getRemoteSocketAddress(), socket.getPort());
    }

    public static void main(String[] args) {
        Client client = new Client();

        System.out.println("connected to the server.");
        client.printConnectionInfo();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String message = getUserInput(scanner);

                System.out.println("sending message to the server...");
                client.sendRequest(message);

                Response response = client.receiveResponse();
                printServerResponse(response);

                if (response.serverResponse().equals(DISCONNECTED_FROM_SERVER_REPLY)) {
                    break;
                }
            }

            client.closeConnection();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private static String getUserInput(Scanner scanner) {
        System.out.print("Enter message: ");
        return scanner.nextLine();
    }

    private static void printServerResponse(Response response) {
        System.out.printf("Server replied : %s%n%n", response.message());
    }
}
