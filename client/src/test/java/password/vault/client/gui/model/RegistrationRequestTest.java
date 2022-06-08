package password.vault.client.gui.model;

import org.junit.Test;

public class RegistrationRequestTest {
    @Test(expected = RegistrationRequest.RegistrationRequestException.class)
    public void testCreatingRequestWithInvalidEmailThrowsException() throws
            RegistrationRequest.RegistrationRequestException {
        String username = "test";
        String email = "blqblq";
        String password = "1234";

        new RegistrationRequest(username, email, password, password, password, password);
    }

    @Test()
    public void testCreatingValidRequestDoesNotThrowException() throws
            RegistrationRequest.RegistrationRequestException {
        String username = "test";
        String email = "test@gmail.com";
        String password = "1234";

        RegistrationRequest registrationRequest = new RegistrationRequest(username, email, password, password,
                                                                          password, password);
    }
}