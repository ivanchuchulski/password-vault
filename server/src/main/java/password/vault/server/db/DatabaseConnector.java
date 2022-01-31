package password.vault.server.db;

import password.vault.server.MyConfig;
import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.cryptography.PasswordHash;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.user.repository.UserNotFoundException;
import password.vault.server.password.vault.CredentialIdentifier;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public boolean insertUser(String username, String email, byte[] passwordHash, byte[] salt) throws
            DatabaseConnectorException {
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
            throw new DatabaseConnectorException("error inserting user", e);
        }
    }

    public boolean loginUser(String username) throws DatabaseConnectorException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DMLQueries.LOGIN_USER.getQueryText())) {
            preparedStatement.setString(1, username);

            int loginSuccess = preparedStatement.executeUpdate();

            if (loginSuccess == 0) {
                return false;
            } else {
                return true;
            }

        } catch (SQLException e) {
            throw new DatabaseConnectorException("unable to log in user");
        }
    }

    public boolean logoutUser(String username) throws DatabaseConnectorException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(DMLQueries.LOGOUT_USER.getQueryText())) {
            preparedStatement.setString(1, username);

            int logoutSuccess = preparedStatement.executeUpdate();

            if (logoutSuccess == 0) {
                return false;
            } else {
                return true;
            }

        } catch (SQLException e) {
            throw new DatabaseConnectorException("unable to log in user");
        }
    }

    public PasswordHash getPasswordForUser(String username) throws UserNotFoundException, DatabaseConnectorException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.SELECT_USER_PASSWORD.getQueryText())) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new UserNotFoundException();
                }
                return new PasswordHash(resultSet.getBytes("password"), resultSet.getBytes("salt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseConnectorException("unable to get user password", e);
        }
    }

    public boolean isUserRegistered(String username) throws DatabaseConnectorException {
        try (PreparedStatement prep = connection.prepareStatement(DMLQueries.SELECT_USER_BY_USERNAME.getQueryText())) {
            prep.setString(1, username);

            try (ResultSet result = prep.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new DatabaseConnectorException("error inserting user", e);
        }
    }

    public boolean isUserLoggedIn(String username) throws DatabaseConnectorException {
        try (PreparedStatement prep = connection.prepareStatement(DMLQueries.SELECT_LOGGED_IN_USER.getQueryText())) {
            prep.setString(1, username);

            try (ResultSet result = prep.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new DatabaseConnectorException("error inserting user", e);
        }
    }

    public boolean insertCredential(String username, String website, String usernameForWebsite,
                                    EncryptedPassword encryptedPassword) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.INSERT_CREDENTIAL.getQueryText())) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, website);
            preparedStatement.setString(3, usernameForWebsite);
            preparedStatement.setBytes(4, encryptedPassword.encryptedPassword());
            preparedStatement.setBytes(5, encryptedPassword.salt());
            preparedStatement.setBytes(6, encryptedPassword.iv());

            int insertSuccess = preparedStatement.executeUpdate();
            return insertSuccess == 1;
        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCredential(String username, CredentialIdentifier credentialIdentifier) throws
            DatabaseConnectorException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.DELETE_CREDENTIAL.getQueryText())) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, credentialIdentifier.website());
            preparedStatement.setString(3, credentialIdentifier.usernameForWebsite());

            int deleteSuccess = preparedStatement.executeUpdate();

            if (deleteSuccess == 0) {
                System.out.println("deleting error username %s".formatted(username));
                return false;
            } else {
                System.out.println("success deleting username %s".formatted(username));
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseConnectorException("unable to delete credential");
        }
    }

    public EncryptedPassword getCredential(String username, CredentialIdentifier credentialIdentifier) throws
            CredentialNotFoundException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.SELECT_CREDENTIAL.getQueryText())) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, credentialIdentifier.website());
            preparedStatement.setString(3, credentialIdentifier.usernameForWebsite());

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

    public boolean isCredentialAdded(String username, CredentialIdentifier credentialIdentifier) throws
            DatabaseConnectorException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.SELECT_CREDENTIAL.getQueryText())) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, credentialIdentifier.website());
            preparedStatement.setString(3, credentialIdentifier.usernameForWebsite());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseConnectorException("unable to select credential");
        }
    }

    public boolean doesUserHaveAnyCredentials(String username) throws
            DatabaseConnectorException {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement(DMLQueries.SELECT_ALL_USERS_CREDENTIAL.getQueryText())) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DatabaseConnectorException("unable to select credential");
        }
    }
}
