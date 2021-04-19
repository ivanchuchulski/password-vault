package bg.sofia.uni.fmi.mjt.password.vault.client;

import bg.sofia.uni.fmi.mjt.password.vault.server.commands.ServerCommand;
import bg.sofia.uni.fmi.mjt.password.vault.server.responses.ServerResponses;

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
    private static final String DISCONNECTED_FROM_SERVER_REPLY = ServerResponses.DISCONNECTED.getResponseText();
    private static final String USER_QUIT_COMMAND = "quit";

    private final SocketChannel socketChannel;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public Client(int serverPort) {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, serverPort));

            reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
        } catch (IOException ioException) {
            throw new RuntimeException("error : connecting to server", ioException);
        }
    }

    public void sendRequest(String request) throws IOException {
        if (socketChannel.isConnected()) {
            writer.println(request);
        }
    }

    public String receiveResponse() throws IOException {
        return reader.readLine();
    }

    public void closeConnection() throws IOException {
        reader.close();
        writer.close();
        socketChannel.close();
    }

    public static void main(String[] args) {
        int serverPort = 7777;
        Client wishListClient = new Client(serverPort);
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

                String reply = wishListClient.receiveResponse();

                System.out.println("server replied : " + reply);

                if (reply.equalsIgnoreCase(DISCONNECTED_FROM_SERVER_REPLY)) {
                    break;
                }

            }
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}
