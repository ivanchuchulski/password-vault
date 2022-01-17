package password.vault.server.db;

import password.vault.server.MyConfig;
import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.exceptions.password.CredentialNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DatabaseConnector {
    private static final String DB_URL = MyConfig.DB_URL;
    private static final String DB_USER = MyConfig.DB_USER;
    private static final String DB_PASSWORD = MyConfig.DB_PASSWORD;

    private Connection connection;

    public DatabaseConnector() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    public boolean insertUser(String username, String email, byte[] passwordHash, byte[] salt) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DMLQueries.INSERT_USER.getQueryText())) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setBytes(3, passwordHash);
            preparedStatement.setBytes(4, salt);

            int insertSuccess = preparedStatement.executeUpdate();

            if (insertSuccess == 0) {
                System.out.println("insert error username %s".formatted(username));
                return false;
            } else {
                System.out.println("insert success username %s".formatted(username));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertCredential(String username, String website, String usernameForWebsite, byte[] pass,
                                    byte[] salt, byte[] iv) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.INSERT_CREDENTIAL.getQueryText())) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, website);
            preparedStatement.setString(3, usernameForWebsite);
            preparedStatement.setBytes(4, pass);
            preparedStatement.setBytes(5, salt);
            preparedStatement.setBytes(6, iv);

            int insertSuccess = preparedStatement.executeUpdate();
            return insertSuccess == 1;
        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public EncryptedPassword getCredential(String username, String website, String usernameForWebsite) throws
            CredentialNotFoundException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.SELECT_CREDENTIAL.getQueryText())) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, website);
            preparedStatement.setString(3, usernameForWebsite);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new CredentialNotFoundException("credential not found");
                }

                return new EncryptedPassword(resultSet.getBytes("password"),
                                             resultSet.getBytes("salt"),
                                             resultSet.getBytes("iv"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new CredentialNotFoundException("credential not found");
        }
    }

    public List<EncryptedPassword> getAllCredentialsForUser(String username) {
        throw new UnsupportedOperationException();
    }
}
