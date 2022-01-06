package password.vault.server.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Objects;

public class PasswordGeneratorResponse {
    private final boolean success;

    @SerializedName("length")
    private final int numberOfSafePasswordsGenerated;

    @SerializedName("passwords")
    private final String[] safePasswordsList;

    public PasswordGeneratorResponse(boolean success, int numberOfSafePasswordsGenerated, String[] safePasswordsList) {
        this.success = success;
        this.numberOfSafePasswordsGenerated = numberOfSafePasswordsGenerated;
        this.safePasswordsList = safePasswordsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordGeneratorResponse that = (PasswordGeneratorResponse) o;
        return success == that.success && numberOfSafePasswordsGenerated == that.numberOfSafePasswordsGenerated && Arrays
                .equals(safePasswordsList, that.safePasswordsList);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(success, numberOfSafePasswordsGenerated);
        result = 31 * result + Arrays.hashCode(safePasswordsList);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String[] getSafePasswordsList() {
        return safePasswordsList;
    }

    public String getPassword() {
        return safePasswordsList[0];
    }
}
