package password.vault.server;

import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.db.DatabaseConnector;
import password.vault.server.exceptions.HashException;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.CredentialIdentifier;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException, HashException, PasswordEncryptorException,
            CredentialNotFoundException {
        startServer();
        // testDB();
    }

    private static void startServer() {
        final int serverPort = 7777;
        final Path usersFilePath = Path.of("resources" + File.separator + "users.txt");
        final Path credentialsFile = Path.of("resources" + File.separator + "credentials.txt");

        final HttpClient httpClientForPasswordSafetyChecker = HttpClient.newBuilder().build();
        final HttpClient httpClientForPasswordGenerator = HttpClient.newBuilder().build();
        final PasswordSafetyChecker passwordSafetyChecker =
                new PasswordSafetyChecker(httpClientForPasswordSafetyChecker);
        final PasswordGenerator passwordGenerator = new PasswordGenerator(httpClientForPasswordGenerator);

        Server server = new Server(serverPort, usersFilePath, credentialsFile, passwordSafetyChecker,
                                   passwordGenerator);
        server.start();
    }

    private static void testDB() throws IOException, HashException, PasswordEncryptorException,
            CredentialNotFoundException {
        DatabaseConnector databaseConnector = new DatabaseConnector();
        String username = "test";
        String email = "test@example.com";
        String pass = "test1234";


        CredentialIdentifier credentialIdentifier = new CredentialIdentifier("facebook.com", "testUsername");
        String sitePass = "passtest1234";

        // testInsertUser(databaseConnector, username, email, pass);
        // testInsertCredential(databaseConnector, username, credentialIdentifier, sitePass);

        EncryptedPassword encryptedPassword = databaseConnector.getCredential(username, credentialIdentifier.website(),
                                                                              credentialIdentifier.usernameForWebsite());

        String decryptPassword = PasswordEncryptor.decryptPassword(encryptedPassword, generateSecretKeyString(username,
                                                                                                              credentialIdentifier,
                                                                                                              encryptedPassword.salt()));
        System.out.println("decrypted pass : " + decryptPassword);
        System.out.println("original pass : " + sitePass);

    }

    private static void testInsertUser(DatabaseConnector databaseConnector, String name, String email, String pass) throws
            IOException, HashException {
        byte[] passBytes = pass.getBytes(StandardCharsets.UTF_8);
        byte[] salt = PasswordEncryptor.generateSalt();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(passBytes);
        outputStream.write(salt);

        byte[] concatenated = outputStream.toByteArray();

        byte[] hashBytes = PasswordHasher.computeSHA512HashWithSalt(concatenated);

        boolean insertSuccess = databaseConnector.insertUser(name, email, hashBytes, salt);

        System.out.println("insert success :" + insertSuccess);
    }

    private static void testInsertCredential(DatabaseConnector databaseConnector, String username,
                                             CredentialIdentifier credentialIdentifier, String sitePass) throws
            PasswordEncryptorException {
        byte[] salt = PasswordEncryptor.generateSalt();
        byte[] ivBytes = PasswordEncryptor.generateSalt();

        // the key can be derived differently
        SecretKey key = generateSecretKeyString(username, credentialIdentifier, salt);

        byte[] encryptedString = PasswordEncryptor.encrypt(sitePass, key, ivBytes);

        boolean insertCredentialSuccess = databaseConnector.insertCredential(username, credentialIdentifier.website(),
                                                                             credentialIdentifier.usernameForWebsite(),
                                                                             encryptedString, salt, ivBytes);

        System.out.println("insertCredentialSuccess : " + insertCredentialSuccess);
    }

    private static SecretKey generateSecretKeyString(String username, CredentialIdentifier credentialIdentifier,
                                                     byte[] salt) throws PasswordEncryptorException {
        return PasswordEncryptor.getKeyFromString(username + credentialIdentifier.usernameForWebsite() + credentialIdentifier.website() + Arrays.toString(salt));
    }

}
