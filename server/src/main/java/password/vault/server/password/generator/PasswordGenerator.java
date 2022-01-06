package password.vault.server.password.generator;

import com.google.gson.Gson;
import password.vault.server.MyConfig;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.exceptions.password.PasswordGeneratorException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
service : https://happi.dev/docs/password-generator
 */
public class PasswordGenerator {
    private static final String API_ENDPOINT_SCHEME = "https";
    private static final String API_ENDPOINT_HOST = "api.happi.dev";
    private static final String API_ENDPOINT_PATH = "/v1/generate-password";
    private static final String API_ENDPOINT_QUERY = "limit=%s&length=%s&num=%s&symbols=%s&upper=%s";

    private static final String AUTHENTICATION_HEADER_NAME = "x-happi-key";
    private static final String AUTHENTICATION_HEADER_VALUE = MyConfig.HAPPY_DEV_AUTH_KEY;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int NUMBER_OF_PASSWORDS_TO_GENERATE = 1;
    private static final boolean INCLUDE_NUMBERS_IN_PASSWORD = true;
    private static final boolean INCLUDE_SYMBOLS_IN_PASSWORD = false;
    private static final boolean INCLUDE_UPPERCASE_LETTERS_IN_PASSWORD = true;

    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;

    public PasswordGenerator(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public PasswordGeneratorResponse generateSafePassword(int passwordLength) throws PasswordGeneratorException {
        if (passwordLength < MIN_PASSWORD_LENGTH) {
            throw new PasswordGeneratorException(String.format("the minimum length for safe password is %s",
                                                               MIN_PASSWORD_LENGTH));
        }
        HttpResponse<String> response;
        try {
            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH,
                              API_ENDPOINT_QUERY
                                      .formatted(NUMBER_OF_PASSWORDS_TO_GENERATE, passwordLength,
                                                 INCLUDE_NUMBERS_IN_PASSWORD, INCLUDE_SYMBOLS_IN_PASSWORD,
                                                 INCLUDE_UPPERCASE_LETTERS_IN_PASSWORD), null);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                                                 .uri(uri)
                                                 .header(AUTHENTICATION_HEADER_NAME, AUTHENTICATION_HEADER_VALUE)
                                                 .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new PasswordGeneratorException("could not retrieve password generation from service", e);
        }

        int statusCode = response.statusCode();

        if (statusCode == HttpURLConnection.HTTP_OK) {
            return GSON.fromJson(response.body(), PasswordGeneratorResponse.class);
        }

        throw new PasswordGeneratorException("unexpected response from password safety service");
    }

    public static int minimumAllowedPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
}
