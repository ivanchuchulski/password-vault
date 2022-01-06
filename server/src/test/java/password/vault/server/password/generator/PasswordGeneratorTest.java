package password.vault.server.password.generator;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.exceptions.password.PasswordGeneratorException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PasswordGeneratorTest {
    @Mock
    private HttpClient httpClientMock;

    @Mock
    private HttpResponse<String> httpResponseMock;

    private static final Gson gson = new Gson();

    private static PasswordGenerator passwordGenerator;

    @Before
    public void setUp() throws IOException, InterruptedException {
        when(httpClientMock.send(Mockito.any(HttpRequest.class),
                                 ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(httpResponseMock);

        passwordGenerator = new PasswordGenerator(httpClientMock);
    }

    @Test
    public void testGenerateSafePasswordGeneratesPasswordWithProvidedLength() throws PasswordGeneratorException {
        boolean success = true;
        int numberOfSafePasswordsGenerated = 1;
        String safePasswordMock = "_rrR~S>k$[8+Ps/x2WyaFv";
        int expectedPasswordLength = safePasswordMock.length();

        PasswordGeneratorResponse expectedPasswordGeneratorResponse =
                new PasswordGeneratorResponse(success, numberOfSafePasswordsGenerated, new String[]{safePasswordMock});

        String passwordGeneratorReportJSON = gson.toJson(expectedPasswordGeneratorResponse);

        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(httpResponseMock.body()).thenReturn(passwordGeneratorReportJSON);

        PasswordGeneratorResponse passwordGeneratorResponse =
                passwordGenerator.generateSafePassword(expectedPasswordLength);

        String safePassword = passwordGeneratorResponse.getSafePasswordsList()[0];
        assertEquals("unexpected response for password generation", expectedPasswordGeneratorResponse,
                     passwordGeneratorResponse);
        assertEquals("the generated password is not the provided length", expectedPasswordLength,
                     safePassword.length());
    }

    @Test(expected = PasswordGeneratorException.class)
    public void testGenerateSafePasswordThrowsExceptionWhenGivenNegativePasswordLength() throws PasswordGeneratorException {
        passwordGenerator.generateSafePassword(-1);
    }

    @Test(expected = PasswordGeneratorException.class)
    public void testGenerateSafePasswordWithNotFoundStatusCodeThrowsException() throws PasswordGeneratorException {
        when(httpResponseMock.statusCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);

        passwordGenerator.generateSafePassword(PasswordGenerator.minimumAllowedPasswordLength());
    }

    @Test
    public void testGenerateSafePasswordWrapsIOExceptionProperly() throws IOException, InterruptedException {
        IOException expectedExc = new IOException();
        when(httpClientMock
                     .send(Mockito.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(expectedExc);

        try {
            passwordGenerator.generateSafePassword(PasswordGenerator.minimumAllowedPasswordLength());
        } catch (Exception actualException) {
            assertEquals(
                    "PasswordGeneratorException should properly wrap the causing IOException or InterruptedException",
                    expectedExc, actualException.getCause());
        }
    }
}