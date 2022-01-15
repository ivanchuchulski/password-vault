package password.vault.server.cryptography;

import java.util.Arrays;

public record EncryptedPassword(byte[] encryptedPassword, byte[] salt, byte[] iv) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncryptedPassword that = (EncryptedPassword) o;
        return Arrays.equals(encryptedPassword, that.encryptedPassword) && Arrays.equals(salt, that.salt) && Arrays.equals(iv, that.iv);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(encryptedPassword);
        result = 31 * result + Arrays.hashCode(salt);
        result = 31 * result + Arrays.hashCode(iv);
        return result;
    }

    @Override
    public String toString() {
        return "EncryptedPassword{" +
                "encryptedPassword=" + Arrays.toString(encryptedPassword) +
                ", salt=" + Arrays.toString(salt) +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }
}
