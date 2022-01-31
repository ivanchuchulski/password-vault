package password.vault.server.db;

public enum DMLQueries {
    SELECT_USER_BY_USERNAME("SELECT username FROM user WHERE username = ?"),

    INSERT_USER("INSERT INTO user(username, email, password, salt) VALUES(?, ?, ?, ?)"),

    SELECT_USER_PASSWORD("SELECT password, salt FROM user WHERE username = ?"),

    LOGIN_USER("INSERT INTO session(username) VALUES(?)"),

    LOGOUT_USER("DELETE FROM session WHERE username = ?"),

    INSERT_CREDENTIAL("INSERT INTO credential(username, website, site_username, password, salt, iv)\n" +
                              "VALUES (?, ?, ?, ?, ?, ?);"),

    SELECT_CREDENTIAL("SELECT password, salt, iv FROM credential WHERE username = ? AND  website = ? AND " +
                              "site_username = ?;"),

    SELECT_ALL_USERS_CREDENTIAL("SELECT * FROM credential WHERE username = ? "),

    DELETE_CREDENTIAL("DELETE FROM credential WHERE username = ? AND website = ? AND site_username = ?"),

    SELECT_LOGGED_IN_USER("SELECT * FROM session WHERE username = ?"),
    ;

    private final String queryText;

    DMLQueries(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryText() {
        return queryText;
    }
}
