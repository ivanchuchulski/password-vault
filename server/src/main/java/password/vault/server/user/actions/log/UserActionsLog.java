package password.vault.server.user.actions.log;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserActionsLog {
    private final Map<String, LocalDateTime> sessions;

    public UserActionsLog() {
        sessions = new HashMap<>();
    }

    public void addUserActionTimeStamp(String username) {
        sessions.put(username, LocalDateTime.now());
    }

    public void removeUserSession(String username) {
        sessions.remove(username);
    }

    public LocalDateTime getLastActionTimeForChannel(String username) {
        return sessions.get(username);
    }
}
