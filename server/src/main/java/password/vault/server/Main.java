package password.vault.server;

import password.vault.server.db.DatabaseConnector;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.password.vault.PasswordVaultDB;
import password.vault.server.password.vault.PasswordVaultInMemory;
import password.vault.server.user.repository.UserRepository;
import password.vault.server.user.repository.UserRepositoryDatabase;
import password.vault.server.user.repository.in.memory.UserRepositoryInMemory;

import java.io.File;
import java.net.http.HttpClient;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        startServer();
    }

    private static void startServer() {
        final Path usersFilePath = Path.of("resources" + File.separator + "users.txt");
        UserRepository userRepositoryInMemory = new UserRepositoryInMemory(usersFilePath);

        final Path credentialsFile = Path.of("resources" + File.separator + "credentials.txt");
        PasswordVault passwordVaultInMemory = new PasswordVaultInMemory(credentialsFile);

        DatabaseConnector databaseConnector = new DatabaseConnector();
        UserRepository userRepositoryDB = new UserRepositoryDatabase(databaseConnector);
        PasswordVault passwordVaultDB = new PasswordVaultDB(databaseConnector);

        final HttpClient httpClientForPasswordSafetyChecker = HttpClient.newBuilder().build();
        final PasswordSafetyChecker passwordSafetyChecker =
                new PasswordSafetyChecker(httpClientForPasswordSafetyChecker);

        final HttpClient httpClientForPasswordGenerator = HttpClient.newBuilder().build();
        final PasswordGenerator passwordGenerator = new PasswordGenerator(httpClientForPasswordGenerator);

        CommandExecutor commandExecutor = new CommandExecutor(userRepositoryDB, passwordVaultDB, passwordSafetyChecker,
                                                              passwordGenerator);
        final int serverPort = 7777;
        Server server = new Server(serverPort, commandExecutor);
        server.start();
    }
}
