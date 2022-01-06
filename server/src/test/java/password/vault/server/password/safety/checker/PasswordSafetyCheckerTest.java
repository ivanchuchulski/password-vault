package password.vault.server.password.safety.checker;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.exceptions.password.PasswordSafetyCheckerException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordSafetyCheckerTest {
    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    private static final Gson gson = new Gson();

    private static PasswordSafetyChecker passwordSafetyChecker;

    private static final String PASSWORD_TO_CHECK = "test_password";

    @Before
    public void setUp() throws IOException, InterruptedException {
        when(httpClientMock.send(Mockito.any(HttpRequest.class),
                                 ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        passwordSafetyChecker = new PasswordSafetyChecker(httpClientMock);
    }

    @Test
    public void testCheckPasswordReturnsPasswordIsUnsafe() throws PasswordSafetyCheckerException {
        boolean passwordWasExposed = true;
        int timesExposed = 10;
        PasswordSafetyResponse expectedPasswordSafetyResponse = new PasswordSafetyResponse(passwordWasExposed,
                                                                                           timesExposed);

        String expectedPasswordSafetyReportJSON = gson.toJson(expectedPasswordSafetyResponse);

        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseMock.body()).thenReturn(expectedPasswordSafetyReportJSON);

        PasswordSafetyResponse passwordSafetyResponse = passwordSafetyChecker.checkPassword(PASSWORD_TO_CHECK);

        assertEquals("unexpected response for password safety", expectedPasswordSafetyResponse, passwordSafetyResponse);
    }

    // the API returns status 404 when the password has not been found in their database
    @Test
    public void testCheckPasswordReturnsPasswordIsSafe() throws PasswordSafetyCheckerException {
        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);

        PasswordSafetyResponse passwordSafetyResponse = passwordSafetyChecker.checkPassword(PASSWORD_TO_CHECK);

        assertFalse("when the response status code is 404 the password safety response should return that password " +
                            "was not exposed ",
                    passwordSafetyResponse.wasPasswordExposed());
    }

    @Test(expected = PasswordSafetyCheckerException.class)
    public void testCheckPasswordThrowsExceptionWhenServiceIsUnavailable() throws PasswordSafetyCheckerException {
        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_UNAVAILABLE);

        passwordSafetyChecker.checkPassword(PASSWORD_TO_CHECK);
    }

    @Test
    public void testCheckPasswordWrapsIOExceptionProperly() throws IOException, InterruptedException {
        IOException expectedExc = new IOException();
        when(httpClientMock
                     .send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(expectedExc);

        try {
            passwordSafetyChecker.checkPassword(PASSWORD_TO_CHECK);
        } catch (Exception actualException) {
            assertEquals(
                    "PasswordSafetyCheckerException should properly wrap the causing IOException",
                    expectedExc, actualException.getCause());
        }
    }
}