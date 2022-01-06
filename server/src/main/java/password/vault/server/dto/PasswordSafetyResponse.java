package password.vault.server.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class PasswordSafetyResponse {
    @SerializedName("revealedInExposure")
    private final boolean passwordWasExposed;

    @SerializedName("exposureCount")
    private final int timesExposed;

    public PasswordSafetyResponse(boolean passwordWasExposed, int timesExposed) {
        this.passwordWasExposed = passwordWasExposed;
        this.timesExposed = timesExposed;
    }

    public boolean wasPasswordExposed() {
        return passwordWasExposed;
    }

    public int getTimesExposed() {
        return timesExposed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordSafetyResponse that = (PasswordSafetyResponse) o;
        return passwordWasExposed == that.passwordWasExposed && timesExposed == that.timesExposed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(passwordWasExposed, timesExposed);
    }

    @Override
    public String toString() {
        return "PasswordSafetyResponse{" +
                "passwordWasExposed=" + passwordWasExposed +
                ", timesExposed=" + timesExposed +
                '}';
    }
}
