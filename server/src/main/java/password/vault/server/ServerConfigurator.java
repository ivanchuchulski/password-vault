package password.vault.server;

import password.vault.server.db.DatabaseConnector;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.password.vault.PasswordVaultDB;
import password.vault.server.user.repository.UserRepository;
import password.vault.server.user.repository.UserRepositoryDatabase;

import java.net.http.HttpClient;

public class ServerConfigurator {

    public static final int SERVER_PORT = 7777;

    public static Server getServer() {
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

        return new Server(SERVER_PORT, commandExecutor);
    }
}
