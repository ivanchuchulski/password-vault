package password.vault.server;

import password.vault.api.ServerCommand;
import password.vault.api.ServerResponses;
import password.vault.server.commands.UserCommand;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.exceptions.InvalidUsernameForSiteException;
import password.vault.server.exceptions.InvalidWebsiteException;
import password.vault.server.exceptions.password.CredentialNotFoundException;
import password.vault.server.exceptions.password.CredentialsAlreadyAddedException;
import password.vault.server.exceptions.password.PasswordEncryptorException;
import password.vault.server.exceptions.password.PasswordGeneratorException;
import password.vault.server.exceptions.password.PasswordSafetyCheckerException;
import password.vault.server.exceptions.password.UsernameNotHavingCredentialsException;
import password.vault.server.exceptions.user.repository.LoginException;
import password.vault.server.exceptions.user.repository.LogoutException;
import password.vault.server.exceptions.user.repository.RegisterException;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.responses.CommandResponse;
import password.vault.server.user.actions.log.UserActionsLog;
import password.vault.server.user.repository.UserRepository;

import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;

public class CommandExecutor {
    private static final int MAX_ALLOWED_USER_INACTIVITY_MINUTES = 1;

    private final UserActionsLog userActionsLog;
    private final ChannelUsernameMapper channelUsernameMapper;

    private final UserRepository userRepository;
    private final PasswordVault passwordVault;

    private final PasswordSafetyChecker passwordSafetyChecker;
    private final PasswordGenerator passwordGenerator;

    public CommandExecutor(UserRepository userRepository, PasswordVault passwordVault,
                           PasswordSafetyChecker passwordSafetyChecker, PasswordGenerator passwordGenerator) {
        this.userRepository = userRepository;
        this.passwordVault = passwordVault;

        this.passwordSafetyChecker = passwordSafetyChecker;
        this.passwordGenerator = passwordGenerator;

        this.userActionsLog = new UserActionsLog();
        this.channelUsernameMapper = new ChannelUsernameMapper();
    }

    public CommandResponse executeCommand(SocketChannel socketChannel, UserCommand clientUserCommand) {
        String userCommand = clientUserCommand.command();
        String[] arguments = clientUserCommand.arguments();

        ServerCommand serverCommand = ServerCommand.getServerCommandFromCommandText(userCommand);

        if (serverCommand.equals(ServerCommand.UNKNOWN)) {
            return new CommandResponse(false, ServerResponses.UNKNOWN_COMMAND.getResponseText());
        }

        if (commandHasIncorrectNumberOfArguments(clientUserCommand, serverCommand)) {
            return new CommandResponse(false, ServerResponses.
                    WRONG_COMMAND_NUMBER_OF_ARGUMENTS.getResponseText().formatted(serverCommand.getCommandOverview()));
        }

        switch (serverCommand) {
            case DISCONNECT:
                return new CommandResponse(true, disconnectUser(socketChannel));
            case REGISTER:
                return new CommandResponse(false, registerUser(socketChannel, arguments));
            case LOGIN:
                return new CommandResponse(false, loginUser(socketChannel, arguments));
            case LOGOUT:
                return new CommandResponse(false, logoutUser(socketChannel));
            case HELP:
                return new CommandResponse(false, ServerCommand.printHelp());
        }

        String username = channelUsernameMapper.getUsernameForChannel(socketChannel);

        if (!isUserLoggedIn(username)) {
            return new CommandResponse(false, ServerResponses.NOT_LOGGED_IN.getResponseText());
        }

        if (!userHasValidSession(username)) {
            logoutUser(socketChannel);
            return new CommandResponse(false, ServerResponses.SESSION_EXPIRED.getResponseText());
        }

        userActionsLog.addUserActionTimeStamp(username);

        String response;
        switch (serverCommand) {
            case ADD_PASSWORD -> response = addPassword(username, arguments);
            case REMOVE_PASSWORD -> response = removePassword(username, arguments);
            case RETRIEVE_CREDENTIALS -> response = retrieveCredentials(username, arguments);
            case GENERATE_PASSWORD -> response = generatePassword(username, arguments);
            default -> response = ServerResponses.UNKNOWN_COMMAND.getResponseText();
        }

        return new CommandResponse(false, response);
    }

    private boolean commandHasIncorrectNumberOfArguments(UserCommand clientUserCommand, ServerCommand serverCommand) {
        return serverCommand.getNumberOfArguments() != clientUserCommand.numberOfArguments();
    }

    private String disconnectUser(SocketChannel socketChannel) {
        try {
            System.out.println("disconnecting client");

            String channelUsername = channelUsernameMapper.getUsernameForChannel(socketChannel);

            userRepository.logOutUser(channelUsername);

            channelUsernameMapper.removeUsernameForChannel(socketChannel);
            userActionsLog.removeUserSession(channelUsername);

            return ServerResponses.DISCONNECTED.getResponseText();
        } catch (LogoutException e) {
            System.out.println("a non-logged in user disconnected");
            return ServerResponses.DISCONNECTED.getResponseText();
        }
    }

    private String registerUser(SocketChannel socketChannel, String[] arguments) {
        try {
            String channelUsername = channelUsernameMapper.getUsernameForChannel(socketChannel);

            if (isUserLoggedIn(channelUsername)) {
                return ServerResponses.REGISTRATION_ERROR.getResponseText().
                        formatted(ServerResponses.ALREADY_LOGGED_IN.getResponseText());
            }

            String username = arguments[0];
            String password = arguments[1];
            String repeatedPassword = arguments[2];

            userRepository.registerUser(username, password, repeatedPassword);

            return ServerResponses.REGISTRATION_SUCCESS.getResponseText().formatted(username);
        } catch (RegisterException registerException) {
            return ServerResponses.REGISTRATION_ERROR.getResponseText().formatted(registerException.getMessage());
        }
    }

    private String loginUser(SocketChannel socketChannel, String[] arguments) {
        try {
            String username = arguments[0];
            String password = arguments[1];

            userRepository.logInUser(username, password);

            channelUsernameMapper.addUsernameForChannel(socketChannel, username);
            userActionsLog.addUserActionTimeStamp(username);

            return ServerResponses.LOGIN_SUCCESS.getResponseText().formatted(username);
        } catch (LoginException loginException) {
            return ServerResponses.LOGIN_ERROR.getResponseText().formatted(loginException.getMessage());
        }
    }

    private String logoutUser(SocketChannel socketChannel) {
        try {
            String usernameForChannel = channelUsernameMapper.getUsernameForChannel(socketChannel);

            userRepository.logOutUser(usernameForChannel);
            channelUsernameMapper.removeUsernameForChannel(socketChannel);
            userActionsLog.removeUserSession(usernameForChannel);

            return ServerResponses.LOGOUT_SUCCESS.getResponseText();
        } catch (LogoutException logoutException) {
            return ServerResponses.LOGOUT_ERROR.getResponseText().formatted(logoutException.getMessage());
        }
    }

    private String addPassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];
            String passwordForSite = arguments[2];

            if (passwordVault.userHasCredentialsForSiteAndUsername(username, website, usernameForSite)) {
                return ServerResponses.CREDENTIAL_GENERATION_ERROR.getResponseText().formatted(username, website,
                                                                                               usernameForSite);
            }

            PasswordSafetyResponse passwordSafetyResponse = passwordSafetyChecker.checkPassword(passwordForSite);

            if (passwordSafetyResponse.wasPasswordExposed()) {
                return ServerResponses.UNSAFE_PASSWORD.getResponseText()
                                                      .formatted(passwordForSite, passwordSafetyResponse
                                                              .getTimesExposed());
            }

            passwordVault.addPassword(username, website, usernameForSite, passwordForSite);

            return ServerResponses.CREDENTIAL_ADDITION_SUCCESS.getResponseText();
        } catch (CredentialsAlreadyAddedException | PasswordEncryptorException e) {
            return ServerResponses.CREDENTIAL_ADDITION_ERROR.getResponseText().formatted(e.getMessage());
        } catch (PasswordSafetyCheckerException e) {
            return ServerResponses.PASSWORD_SAFETY_SERVICE_ERROR.getResponseText().formatted(e.getMessage());
        } catch (InvalidWebsiteException | InvalidUsernameForSiteException e) {
            return ServerResponses.WRONG_COMMAND_ARGUMENT.getResponseText().formatted(e.getMessage());
        }
    }

    private String removePassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];

            passwordVault.removePassword(username, website, usernameForSite);

            return ServerResponses.CREDENTIAL_REMOVAL_SUCCESS.getResponseText().formatted(website, usernameForSite);
        } catch (UsernameNotHavingCredentialsException | CredentialNotFoundException e) {
            return ServerResponses.CREDENTIAL_REMOVAL_ERROR.getResponseText().formatted(e.getMessage());
        }
    }

    private String retrieveCredentials(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];

            String retrievedPassword = passwordVault.retrieveCredentials(username, website, usernameForSite);

            return ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS.getResponseText().formatted(retrievedPassword);
        } catch (UsernameNotHavingCredentialsException | CredentialNotFoundException | PasswordEncryptorException e) {
            return ServerResponses.CREDENTIAL_RETRIEVAL_ERROR.getResponseText().formatted(e.getMessage());
        }
    }

    private String generatePassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];
            int passwordLength = Integer.parseInt(arguments[2]);

            if (passwordVault.userHasCredentialsForSiteAndUsername(username, website, usernameForSite)) {
                return ServerResponses.CREDENTIAL_GENERATION_ERROR.getResponseText().formatted(username, website,
                                                                                               usernameForSite);
            }

            PasswordGeneratorResponse passwordGeneratorResponse =
                    passwordGenerator.generateSafePassword(passwordLength);

            if (!passwordGeneratorResponse.isSuccess()) {
                return ServerResponses.PASSWORD_GENERATION_SERVICE_ERROR.getResponseText();
            }

            String generatedPassword = passwordGeneratorResponse.getPassword();

            passwordVault.addPassword(username, website, usernameForSite, generatedPassword);

            return ServerResponses.CREDENTIAL_GENERATION_SUCCESS.getResponseText().formatted(website, usernameForSite
                    , generatedPassword);
        } catch (PasswordGeneratorException | PasswordEncryptorException | CredentialsAlreadyAddedException e) {
            return String.format("%s : %s", "error generating password", e);
        } catch (NumberFormatException e) {
            return ServerResponses.WRONG_COMMAND_NUMBER_OF_ARGUMENTS.getResponseText();
        } catch (InvalidWebsiteException | InvalidUsernameForSiteException e) {
            return ServerResponses.WRONG_COMMAND_ARGUMENT.getResponseText().formatted(e.getMessage());
        }
    }

    private boolean isUserLoggedIn(String username) {
        return userRepository.isUsernameLoggedIn(username);
    }

    private boolean userHasValidSession(String username) {
        LocalDateTime lastActionTimeForChannel = userActionsLog.getLastActionTimeForChannel(username);

        LocalDateTime minimumMinutesAgo = LocalDateTime.now().minusMinutes(MAX_ALLOWED_USER_INACTIVITY_MINUTES);

        return lastActionTimeForChannel.isAfter(minimumMinutesAgo);
    }
}
