package password.vault.client.console;

import password.vault.api.Response;
import password.vault.api.ServerResponses;
import password.vault.client.communication.Client;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleClient {
    private static final ServerResponses DISCONNECTED_FROM_SERVER_REPLY = ServerResponses.DISCONNECTED;
    private final Client client;

    public ConsoleClient(Client client) {
        this.client = client;
    }

    public void run() {
        System.out.println("connected to the server successfully!");
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

