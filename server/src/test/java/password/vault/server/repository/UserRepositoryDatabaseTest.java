package password.vault.server.repository;

import org.junit.Test;
import org.mockito.Mockito;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.requests.RegistrationRequest;
import password.vault.server.user.repository.UserRepository;
import password.vault.server.user.repository.UserRepositoryDatabase;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

public class UserRepositoryDatabaseTest {

    @Test
    public void testRegisteringNonRegisteredUsernameRegistersIt() throws PasswordHasher.HashException,
            DatabaseConnectorException, UserRepository.InvalidUsernameException, UserRepository.RegisterException,
            UserRepository.UserAlreadyRegisteredException {
        DatabaseConnector databaseConnectorMock = Mockito.mock(DatabaseConnector.class);

        UserRepository userRepository = new UserRepositoryDatabase(databaseConnectorMock);

        RegistrationRequest registrationRequest = new RegistrationRequest("example", "ivan@example.com", "1235",
                                                                          "4567");

        when(databaseConnectorMock.insertUser(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn(true);

        userRepository.registerUser(registrationRequest.username(), registrationRequest.email(),
                                    registrationRequest.password(), registrationRequest.masterPassword());

        when(databaseConnectorMock.isUserRegistered(registrationRequest.username())).thenReturn(true);

        boolean usernameRegistered = userRepository.isUsernameRegistered(registrationRequest.username());

        assertTrue(usernameRegistered);
    }
}