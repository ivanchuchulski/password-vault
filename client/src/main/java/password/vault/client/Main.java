package password.vault.client;

public class Main {
    public static void main(String[] args) {
        Client client = new Client();
        ConsoleClient consoleClient = new ConsoleClient(client);
        consoleClient.run();
    }
}
