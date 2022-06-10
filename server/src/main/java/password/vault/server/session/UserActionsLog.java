package password.vault.server.session;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class UserActionsLog {
    private static final int MAX_ALLOWED_USER_INACTIVITY_MINUTES = 10;

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

    public boolean userHasValidSession(String username) {
        LocalDateTime lastActionTimeForChannel = getLastActionTimeForChannel(username);

        LocalDateTime minimumMinutesAgo = LocalDateTime.now().minusMinutes(MAX_ALLOWED_USER_INACTIVITY_MINUTES);

        return lastActionTimeForChannel.isAfter(minimumMinutesAgo);
    }
}
