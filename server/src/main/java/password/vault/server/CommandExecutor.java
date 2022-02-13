package password.vault.server;

import password.vault.api.Response;
import password.vault.api.ServerCommand;
import password.vault.api.ServerResponses;
import password.vault.server.communication.CommandResponse;
import password.vault.server.communication.UserRequest;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.db.DatabaseConnectorException;
import password.vault.server.dto.PasswordGeneratorResponse;
import password.vault.server.dto.PasswordSafetyResponse;
import password.vault.server.password.generator.PasswordGenerator;
import password.vault.server.password.safety.checker.PasswordSafetyChecker;
import password.vault.server.password.vault.CredentialIdentifier;
import password.vault.server.password.vault.PasswordVault;
import password.vault.server.password.vault.PasswordVaultDB;
import password.vault.server.password.vault.WebsiteCredential;
import password.vault.server.session.ChannelUsernameMapper;
import password.vault.server.session.UserActionsLog;
import password.vault.server.user.repository.UserRepository;

import java.util.List;

public class CommandExecutor {
    private static final String SAMPLE_MASTER_PASSWORD = "1234";
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

    public CommandResponse executeCommand(UserRequest userRequest) {
        ServerCommand serverCommand = ServerCommand.getServerCommandFromCommandText(userRequest.command());

        if (serverCommand.equals(ServerCommand.UNKNOWN)) {
            return new CommandResponse(false,
                                       new Response(ServerResponses.UNKNOWN_COMMAND,
                                                    "command %s you entered is unknown command".formatted(userRequest.command())));
        }

        if (commandHasIncorrectNumberOfArguments(userRequest, serverCommand)) {
            return new CommandResponse(false,
                                       new Response(ServerResponses.WRONG_COMMAND_NUMBER_OF_ARGUMENTS, "your command " +
                                               "has incorrect number of arguments"));
        }

        if (serverCommand.equals(ServerCommand.DISCONNECT)) {
            Response response = disconnectUser(userRequest);
            return new CommandResponse(true, response);
        }

        switch (serverCommand) {
            case REGISTER:
                return new CommandResponse(false, registerUser(userRequest));
            case LOGIN:
                return new CommandResponse(false, loginUser(userRequest));
            case LOGOUT:
                return new CommandResponse(false, logoutUser(userRequest));
            case HELP:
                return new CommandResponse(false, new Response(ServerResponses.HELP_COMMAND,
                                                               ServerCommand.printHelp()));
        }

        String username = channelUsernameMapper.getUsernameForChannel(userRequest.getSocketChannel());

        if (!userRepository.isUsernameLoggedIn(username)) {
            return new CommandResponse(false, new Response(ServerResponses.NOT_LOGGED_IN, "you are not logged in"));
        }

        if (!userActionsLog.userHasValidSession(username)) {
            logoutUser(userRequest);
            return new CommandResponse(false, new Response(ServerResponses.SESSION_EXPIRED,
                                                           "your session has expired, you have to log in again"));
        }

        userActionsLog.addUserActionTimeStamp(username);

        Response response =
                switch (serverCommand) {
                    case ADD_PASSWORD -> addPassword(username, userRequest.arguments());
                    case REMOVE_PASSWORD -> removePassword(username, userRequest.arguments());
                    case RETRIEVE_CREDENTIALS -> retrieveCredentials(username, userRequest.arguments());
                    case GENERATE_PASSWORD -> generatePassword(username, userRequest.arguments());
                    case GET_ALL_CREDENTIALS -> getAllCredentials(username);
                    case CHECK_PASSWORD_SAFETY -> checkPasswordSafety(userRequest.arguments());
                    default -> new Response(ServerResponses.HELP_COMMAND, ServerCommand.printHelp());
                };

        return new CommandResponse(false, response);
    }

    private boolean commandHasIncorrectNumberOfArguments(UserRequest clientUserRequest, ServerCommand serverCommand) {
        return serverCommand.getNumberOfArguments() != clientUserRequest.numberOfArguments();
    }

    private Response disconnectUser(UserRequest userRequest) {
        try {
            System.out.println("disconnecting client");

            String channelUsername = channelUsernameMapper.getUsernameForChannel(userRequest.getSocketChannel());

            userRepository.logOutUser(channelUsername);
            channelUsernameMapper.removeUsernameForChannel(userRequest.getSocketChannel());
            userActionsLog.removeUserSession(channelUsername);

            return new Response(ServerResponses.DISCONNECTED, "successfully logged out");
        } catch (UserRepository.UserNotLoggedInException e) {
            System.out.println("a non-logged in user disconnected");
            return new Response(ServerResponses.DISCONNECTED, "successfully logged out");
        } catch (UserRepository.LogoutException e) {
            return new Response(ServerResponses.LOGOUT_ERROR, "unable to process logout request");
        }
    }

    private Response registerUser(UserRequest userRequest) {
        try {
            String username = userRequest.arguments()[0];
            String email = userRequest.arguments()[1];
            String password = userRequest.arguments()[2];
            String repeatedPassword = userRequest.arguments()[3];

            if (!password.equals(repeatedPassword)) {
                return new Response(ServerResponses.PASSWORD_DO_NOT_MATCH, "passwords do not match");
            }

            userRepository.registerUser(username, password, email);

            return new Response(ServerResponses.REGISTRATION_SUCCESS,
                                "username %s registered successfully".formatted(username));
        } catch (UserRepository.InvalidUsernameException e) {
            return new Response(ServerResponses.REGISTRATION_ERROR, "invalid username provided");
        } catch (UserRepository.UserAlreadyRegisteredException e) {
            return new Response(ServerResponses.REGISTRATION_ERROR, "user already registered");
        } catch (PasswordEncryptor.HashException | DatabaseConnectorException | UserRepository.RegisterException e) {
            return new Response(ServerResponses.REGISTRATION_ERROR, "unable to complete your request, try again");
        }
    }

    private Response loginUser(UserRequest userRequest) {
        try {
            String username = userRequest.arguments()[0];
            String password = userRequest.arguments()[1];

            userRepository.logInUser(username, password);

            channelUsernameMapper.addUsernameForChannel(userRequest.getSocketChannel(), username);
            userActionsLog.addUserActionTimeStamp(username);

            return new Response(ServerResponses.LOGIN_SUCCESS, "sucess logging in user %s".formatted(username));
        } catch (UserRepository.LoginException | PasswordEncryptor.HashException e) {
            return new Response(ServerResponses.LOGIN_ERROR, "unable to complete your logout request, try again ");
        } catch (UserRepository.UserAlreadyLoggedInException e) {
            return new Response(ServerResponses.USER_ALREADY_LOGGED, "you are already logged in");
        } catch (UserRepository.UserNotFoundException e) {
            return new Response(ServerResponses.USER_DOES_NOT_EXIST, "wrong username/password combination");
        }
    }

    private Response logoutUser(UserRequest userRequest) {
        try {
            String usernameForChannel = channelUsernameMapper.getUsernameForChannel(userRequest.getSocketChannel());

            userRepository.logOutUser(usernameForChannel);
            channelUsernameMapper.removeUsernameForChannel(userRequest.getSocketChannel());
            userActionsLog.removeUserSession(usernameForChannel);

            return new Response(ServerResponses.LOGOUT_SUCCESS, "success logging out");
        } catch (UserRepository.UserNotLoggedInException e) {
            return new Response(ServerResponses.USER_NOT_LOGGED_IN, "you are not logged in");
        } catch (UserRepository.LogoutException e) {
            return new Response(ServerResponses.LOGOUT_ERROR, "unable to process logout request");
        }
    }

    private Response addPassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];
            String passwordForSite = arguments[2];

            PasswordSafetyResponse passwordSafetyResponse = passwordSafetyChecker.checkPassword(passwordForSite);

            if (passwordSafetyResponse.wasPasswordExposed()) {
                return new Response(ServerResponses.UNSAFE_PASSWORD,
                                    "password was exposed %d times".formatted(passwordSafetyResponse.getTimesExposed()));
            }

            WebsiteCredential websiteCredential = new WebsiteCredential(website, usernameForSite, passwordForSite);
            passwordVault.addPassword(username, websiteCredential, SAMPLE_MASTER_PASSWORD);

            return new Response(ServerResponses.CREDENTIAL_ADDITION_SUCCESS, "added password successfully");
        } catch (PasswordVaultDB.CredentialsAlreadyAddedException e) {
            return new Response(ServerResponses.CREDENTIAL_ADDITION_ERROR, "credential already added");
        } catch (PasswordEncryptor.PasswordEncryptorException | PasswordSafetyChecker.PasswordSafetyCheckerException | DatabaseConnectorException e) {
            return new Response(ServerResponses.CREDENTIAL_ADDITION_ERROR, "couldn't complete your request, try again");
        } catch (WebsiteCredential.InvalidWebsiteException e) {
            return new Response(ServerResponses.WRONG_COMMAND_ARGUMENT, "website is invalid");
        } catch (WebsiteCredential.InvalidUsernameForSiteException e) {
            return new Response(ServerResponses.WRONG_COMMAND_ARGUMENT, "username is invalid");
        }
    }

    private Response removePassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];

            passwordVault.removePassword(username, website, usernameForSite, SAMPLE_MASTER_PASSWORD);

            return new Response(ServerResponses.CREDENTIAL_REMOVAL_SUCCESS, "credentials removed");
        } catch (PasswordVault.UsernameNotHavingCredentialsException e) {
            return new Response(ServerResponses.NO_CREDENTIALS_ADDED, "you don't have any credential");
        } catch (PasswordVault.CredentialNotFoundException e) {
            return new Response(ServerResponses.NO_SUCH_CREDENTIAL, "no such credential");
        } catch (DatabaseConnectorException | PasswordVaultDB.CredentialRemovalException e) {
            return new Response(ServerResponses.CREDENTIAL_REMOVAL_ERROR, "couldn't complete your request, try again");
        }
    }

    private Response retrieveCredentials(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];

            String retrievedPassword = passwordVault.retrieveCredentials(username, website, usernameForSite,
                                                                         SAMPLE_MASTER_PASSWORD);

            return new Response(ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS, retrievedPassword);
        } catch (PasswordVault.UsernameNotHavingCredentialsException e) {
            return new Response(ServerResponses.NO_CREDENTIALS_ADDED, "you don't have any credentials");
        } catch (PasswordVault.CredentialNotFoundException e) {
            return new Response(ServerResponses.NO_SUCH_CREDENTIAL, "no such credential");
        } catch (PasswordEncryptor.PasswordEncryptorException | DatabaseConnectorException e) {
            return new Response(ServerResponses.CREDENTIAL_RETRIEVAL_ERROR, "unable to retrieve credential, try again");
        }
    }

    private Response getAllCredentials(String username) {
        try {
            List<CredentialIdentifier> allCredentials = passwordVault.getAllCredentials(username);

            StringBuilder sb = new StringBuilder();
            sb.append("user %s credentials : %n".formatted(username));
            for (CredentialIdentifier credentialIdentifier : allCredentials) {
                String format = String.format("username \"%s\" for website \"%s\"%n",
                                              credentialIdentifier.usernameForWebsite(),
                                              credentialIdentifier.website());
                sb.append(format);
            }

            return new Response(ServerResponses.CREDENTIAL_RETRIEVAL_SUCCESS, sb.toString());
        } catch (PasswordVault.UsernameNotHavingCredentialsException e) {
            return new Response(ServerResponses.CREDENTIAL_REMOVAL_ERROR, "you do not have any credentials added");
        } catch (DatabaseConnectorException e) {
            return new Response(ServerResponses.CREDENTIAL_RETRIEVAL_ERROR, "unable to retrieve credential, try again");
        }
    }


    private Response generatePassword(String username, String[] arguments) {
        try {
            String website = arguments[0];
            String usernameForSite = arguments[1];
            int passwordLength = Integer.parseInt(arguments[2]);

            if (passwordVault.userHasCredentialsForSiteAndUsername(username, website, usernameForSite)) {
                return new Response(ServerResponses.CREDENTIAL_ADDITION_ERROR, "credential already added");
            }

            PasswordGeneratorResponse passwordGeneratorResponse =
                    passwordGenerator.generateSafePassword(passwordLength);

            if (!passwordGeneratorResponse.isSuccess()) {
                return new Response(ServerResponses.PASSWORD_GENERATION_ERROR, "unable to generate password, " +
                        "try again;");
            }

            String generatedPassword = passwordGeneratorResponse.getPassword();

            passwordVault.addPassword(username, new WebsiteCredential(website, usernameForSite, generatedPassword),
                                      SAMPLE_MASTER_PASSWORD);

            return new Response(ServerResponses.CREDENTIAL_GENERATION_SUCCESS, generatedPassword);
        } catch (PasswordGenerator.PasswordGeneratorException | PasswordEncryptor.PasswordEncryptorException | DatabaseConnectorException e) {
            return new Response(ServerResponses.PASSWORD_GENERATION_ERROR, "unable to generate password");
        } catch (PasswordVaultDB.CredentialsAlreadyAddedException e) {
            return new Response(ServerResponses.CREDENTIAL_ADDITION_ERROR, "credential already added");
        } catch (NumberFormatException e) {
            return new Response(ServerResponses.WRONG_COMMAND_NUMBER_OF_ARGUMENTS, "incorrect arguments");
        } catch (WebsiteCredential.InvalidWebsiteException e) {
            return new Response(ServerResponses.WRONG_COMMAND_ARGUMENT, "website is invalid");
        } catch (WebsiteCredential.InvalidUsernameForSiteException e) {
            return new Response(ServerResponses.WRONG_COMMAND_ARGUMENT, "username is invalid");
        }
    }

    private Response checkPasswordSafety(String[] arguments) {
        try {
            String password = arguments[0];
            PasswordSafetyResponse passwordSafetyResponse = passwordSafetyChecker.checkPassword(password);

            if (passwordSafetyResponse.wasPasswordExposed()) {
                return new Response(ServerResponses.UNSAFE_PASSWORD,
                                    "the password is unsafe, it was was exposed %d times".formatted(passwordSafetyResponse.getTimesExposed()));

            }

            return new Response(ServerResponses.SAFE_PASSWORD, "password is safe, we found zero exposures");
        } catch (PasswordSafetyChecker.PasswordSafetyCheckerException e) {
            return new Response(ServerResponses.PASSWORD_SAFETY_SERVICE_ERROR,
                                "unable to complete your request, please try again later");
        }
    }
}
