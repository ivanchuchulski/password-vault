package password.vault.server.password.safety.checker;

import com.google.gson.Gson;
import password.vault.server.MyConfig;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.exceptions.password.PasswordSafetyCheckerException;
import password.vault.server.cryptography.PasswordHasher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PasswordSafetyChecker {
    private static final String API_ENDPOINT_SCHEME = "https";
    private static final String API_ENDPOINT_HOST = "api.enzoic.com";
    private static final String API_ENDPOINT_PATH = "/passwords";
    private static final String API_ENDPOINT_QUERY = "sha1=%s&md5=%s&sha256=%s";

    private static final String AUTHENTICATION_HEADER_NAME = "Authorization";
    private static final String AUTHENTICATION_HEADER_VALUE = "basic " + MyConfig.ENZOIC_AUTH_KEY;

    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;

    public PasswordSafetyChecker(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public PasswordSafetyResponse checkPassword(String password) throws PasswordSafetyCheckerException {
        HttpResponse<String> response;
        try {
            String hashWithMD5 = PasswordHasher.computeHash(password, PasswordHasher.MD5_MESSAGE_DIGEST_INSTANCE);
            String hashWithSHA1 = PasswordHasher.computeHash(password, PasswordHasher.SHA1_MESSAGE_DIGEST_INSTANCE);
            String hashWithSHA256 = PasswordHasher.computeHash(password, PasswordHasher.SHA256_MESSAGE_DIGEST_INSTANCE);

            URI uri = new URI(API_ENDPOINT_SCHEME, API_ENDPOINT_HOST, API_ENDPOINT_PATH,
                              API_ENDPOINT_QUERY.formatted(hashWithSHA1, hashWithMD5, hashWithSHA256), null);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                                                 .uri(uri)
                                                 .header(AUTHENTICATION_HEADER_NAME, AUTHENTICATION_HEADER_VALUE)
                                                 .build();

            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new PasswordSafetyCheckerException("could not retrieve password safety report from service",
                                                     e);
        }

        int statusCode = response.statusCode();

        if (statusCode == HttpURLConnection.HTTP_OK) {
            return GSON.fromJson(response.body(), PasswordSafetyResponse.class);
        }

        /*
            when the status code is 404
            No candidate matches were found in the database of compromised passwords – indicates password is not
            known to be compromised.
        */
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return new PasswordSafetyResponse(false, 0);
        }

        throw new PasswordSafetyCheckerException("unexpected response from password safety service");
    }
}
