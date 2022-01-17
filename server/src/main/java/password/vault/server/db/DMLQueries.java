package password.vault.server.db;

public enum DMLQueries {
    SELECT_USER_BY_USERNAME("SELECT username FROM user WHERE username = ?"),

    INSERT_USER("INSERT INTO user(username, email, password, salt) VALUES(?, ?, ?, ?)"),

    INSERT_CREDENTIAL("INSERT INTO credential(username, website, site_username, password, salt, iv)\n" +
                              "VALUES (?, ?, ?, ?, ?, ?);"),

    SELECT_CREDENTIAL("SELECT password, salt, iv FROM credential WHERE username = ? AND  website = ? AND " +
                              "site_username = ?;");

    private final String queryText;

    DMLQueries(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryText() {
        return queryText;
    }
}
