package password.vault.server.password.vault;

import com.google.gson.Gson;
import password.vault.server.dto.CredentialDTO;
import password.vault.server.exceptions.InvalidUsernameForSiteException;
import password.vault.server.exceptions.InvalidWebsiteException;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.CredentialsAlreadyAddedException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.exceptions.password.UsernameNotHavingCredentialsException;
import password.vault.server.cryptography.PasswordEncryptor;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PasswordVaultInMemory implements PasswordVault {
    private final static String WEBSITE_PATTERN = "[a-zA-Z0-9]+\\.[a-zA-Z]+";
    private final static String USERNAME_FOR_SITE_PATTERN = "[a-zA-Z0-9-_]{3,}";

    // { username -> { website, usernameForWebsite -> password }}
    private final Map<String, UserCredentials> credentialsForUser;
    private final Path credentialsFile;

    private final static Gson gson = new Gson();

    public PasswordVaultInMemory(Path credentialsFile) {
        credentialsForUser = new HashMap<>();
        this.credentialsFile = credentialsFile;

        createCredentialsFile();

        List<CredentialDTO> credentialDTOS = readAllCredentialsFromFile();

        addCredentials(credentialDTOS);
    }

    @Override
    public void addPassword(String username, String website, String usernameForSite, String password)
            throws CredentialsAlreadyAddedException, PasswordEncryptorException,
            InvalidUsernameForSiteException, InvalidWebsiteException {
        if (!isWebsiteValid(website)) {
            throw new InvalidWebsiteException(String.format("website %s is invalid", website));
        }

        if (!isUsernameForSiteValid(usernameForSite)) {
            throw new InvalidUsernameForSiteException(String.format("username for site %s is invalid",
                                                                    usernameForSite));
        }

        if (!credentialsForUser.containsKey(username)) {
            credentialsForUser.put(username, new UserCredentials());
        }

        UserCredentials userCredentials = credentialsForUser.get(username);

        CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);

        if (userCredentials.containsCredential(credentialIdentifier)) {
            throw new CredentialsAlreadyAddedException(
                    String.format("user %s has already added credentials for website %s and username %s",
                                  username, website, usernameForSite));
        }

        String encryptedPassword = encryptPassword(password, buildKeyDerivationString(username, usernameForSite));

        userCredentials.addCredential(credentialIdentifier, encryptedPassword);

        writeCredentialToFile(new CredentialDTO(username, website, usernameForSite, encryptedPassword));
    }

    @Override
    public void removePassword(String username, String website, String usernameForSite)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException {
        UserCredentials userCredentials = getCredentialsForUser(username);

        CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);

        if (!userCredentials.containsCredential(credentialIdentifier)) {
            throw new CredentialNotFoundException(
                    String.format("user %s does not have credentials for website %s and username %s",
                                  username, website, usernameForSite));
        }

        String encryptedPassword = userCredentials.getEncryptedPassword(credentialIdentifier);

        userCredentials.removeCredential(credentialIdentifier);

        removeCredentialFromFile(new CredentialDTO(username, website, usernameForSite, encryptedPassword));
    }


    @Override
    public String retrieveCredentials(String username, String website, String usernameForSite)
            throws UsernameNotHavingCredentialsException, CredentialNotFoundException, PasswordEncryptorException {
        UserCredentials userCredentials = getCredentialsForUser(username);

        CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);

        if (!userCredentials.containsCredential(credentialIdentifier)) {
            throw new CredentialNotFoundException(
                    String.format("user %s does not have credentials for website %s and username %s",
                                  username, website, usernameForSite));
        }


        String encryptedPassword = userCredentials.getEncryptedPassword(credentialIdentifier);
        SecretKey secretKey =
                PasswordEncryptor.getKeyFromString(buildKeyDerivationString(username, usernameForSite));

        return PasswordEncryptor.decrypt(encryptedPassword, secretKey);
    }


    @Override
    public boolean userHasCredentialsForSiteAndUsername(String username, String website, String usernameForSite) {
        try {
            retrieveCredentials(username, website, usernameForSite);
            return true;
        } catch (UsernameNotHavingCredentialsException | CredentialNotFoundException | PasswordEncryptorException e) {
            return false;
        }
    }

    private boolean isWebsiteValid(String website) {
        return website.matches(WEBSITE_PATTERN);
    }

    private boolean isUsernameForSiteValid(String usernameForSite) {
        return usernameForSite.matches(USERNAME_FOR_SITE_PATTERN);
    }

    private UserCredentials getCredentialsForUser(String username) throws UsernameNotHavingCredentialsException {
        if (!credentialsForUser.containsKey(username)) {
            throw new UsernameNotHavingCredentialsException(
                    String.format("user %s does not have any credentials added", username));
        }

        return credentialsForUser.get(username);
    }

    private String buildKeyDerivationString(String username, String usernameForSite) {
        return username + usernameForSite;
    }

    private String encryptPassword(String password, String keyDerivationString) throws PasswordEncryptorException {
        SecretKey secretKey = PasswordEncryptor.getKeyFromString(keyDerivationString);

        return PasswordEncryptor.encrypt(password, secretKey);
    }

    private void createCredentialsFile() {
        try {
            Files.createFile(credentialsFile);
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
            System.out.println("credentials file already exists!");
        } catch (IOException ioException) {
            throw new RuntimeException("error : creating credentials file", ioException);
        }
    }

    private List<CredentialDTO> readAllCredentialsFromFile() {
        try (BufferedReader bufferedReader =
                     new BufferedReader(new FileReader(String.valueOf(credentialsFile)))) {

            List<CredentialDTO> credentialDTOS = new ArrayList<>();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                CredentialDTO credentialDTO = gson.fromJson(line, CredentialDTO.class);

                System.out.println("credential");
                System.out.println(credentialDTO.toString());
                credentialDTOS.add(credentialDTO);
            }

            return credentialDTOS;
        } catch (IOException ioException) {
            throw new RuntimeException("error : writing users to file", ioException);
        }
    }

    private void addCredentials(List<CredentialDTO> credentialDTOS) {
        for (CredentialDTO credentialDTO : credentialDTOS) {
            String username = credentialDTO.getUsername();

            if (!credentialsForUser.containsKey(username)) {
                credentialsForUser.put(username, new UserCredentials());
            }

            UserCredentials userCredentials = credentialsForUser.get(username);

            String website = credentialDTO.getWebsite();
            String usernameForSite = credentialDTO.getUsernameForSite();
            String encryptedPassword = credentialDTO.getEncryptedPassword();
            CredentialIdentifier credentialIdentifier = new CredentialIdentifier(website, usernameForSite);

            userCredentials.addCredential(credentialIdentifier, encryptedPassword);
        }
    }

    private void writeCredentialToFile(CredentialDTO credentialDTO) {
        try (BufferedWriter bufferedWriter =
                     new BufferedWriter(new FileWriter(String.valueOf(credentialsFile), true))) {

            String credentialAsJSON = gson.toJson(credentialDTO);

            bufferedWriter.write(credentialAsJSON);
            bufferedWriter.write(System.lineSeparator());
            bufferedWriter.flush();
        } catch (IOException ioException) {
            throw new RuntimeException("error : writing credential to file", ioException);
        }
    }


    private void removeCredentialFromFile(CredentialDTO credentialDTOToRemove) {
        try {
            List<String> credentials = Files.readAllLines(credentialsFile, StandardCharsets.UTF_8);
            String credentialToRemoveAsJSON = gson.toJson(credentialDTOToRemove);

            List<String> updatedCredentials =
                    credentials.stream()
                               .filter(credential -> !credentialToRemoveAsJSON.equals(credential))
                               .collect(Collectors.toCollection(LinkedList::new));

            // deleting the contents of the file
            FileChannel.open(credentialsFile, StandardOpenOption.WRITE).truncate(0).close();

            Files.write(credentialsFile, updatedCredentials, StandardCharsets.UTF_8);

        } catch (IOException ioException) {
            throw new RuntimeException("error : writing users to file", ioException);
        }
    }
}

