package password.vault.api;

public enum ServerResponses {
    UNKNOWN_COMMAND("[ error : unknown command ]"),
    WRONG_COMMAND_NUMBER_OF_ARGUMENTS("[ error : wrong number of arguments, correct is %s ]"),
    WRONG_COMMAND_ARGUMENT("[ error : wrong command argument : %s ]"),
    SESSION_EXPIRED("[ error : your session has expired, logging you out ]"),
    DISCONNECTED("[ success : disconnected from server ]"),

    NOT_LOGGED_IN("[ error : you are not logged in ]"),
    ALREADY_LOGGED_IN("you are already logged in"),

    REGISTRATION_SUCCESS("[ success : username %s successfully registered ]"),
    REGISTRATION_ERROR("[ error registering : %s ]"),

    LOGIN_SUCCESS("[ success : user %s successfully logged in ]"),
    LOGIN_ERROR("[ error logging in : %s ]"),

    LOGOUT_SUCCESS("[ success : logging out ]"),
    LOGOUT_ERROR("[ error logging out : %s ]"),

    CREDENTIAL_ADDITION_SUCCESS("[ success added credentials ]"),
    CREDENTIAL_ADDITION_ERROR("[ error adding credentials : %s]"),
    UNSAFE_PASSWORD("[ error adding password : password %s is unsafe, it was exposed %s times ]"),

    CREDENTIAL_REMOVAL_SUCCESS("[ success removed credentials for site %s and username %s ]"),
    CREDENTIAL_REMOVAL_ERROR("[ error removing credentials : %s ]"),

    CREDENTIAL_RETRIEVAL_SUCCESS("[ success retrieving credentials : password is %s ]"),
    CREDENTIAL_RETRIEVAL_ERROR("[ error retrieving credentials : %s ]"),

    PASSWORD_SAFETY_SERVICE_ERROR("[ error checking password safety : %s]"),

    CREDENTIAL_GENERATION_SUCCESS("[ success generated credentials : website %s usernameForSite %s password %s ]"),
    CREDENTIAL_GENERATION_ERROR("[ error generating credentials : you already have password for site %s and username" +
                                        " %s ]"),

    PASSWORD_GENERATION_ERROR("[ error generating safe password safety ]"),

    USER_ALREADY_LOGGED(""),
    USER_NOT_LOGGED_IN(""),
    PASSWORD_DO_NOT_MATCH(""),
    NO_SUCH_CREDENTIAL(""),
    NO_CREDENTIALS_ADDED(""),
    HELP_COMMAND("");

    private final String responseText;

    ServerResponses(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }
}
