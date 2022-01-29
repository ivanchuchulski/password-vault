package password.vault.server.cryptography;

import password.vault.server.exceptions.HashException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PasswordHash {
    private final byte[] passwordBytes;
    private final byte[] salt;

    public PasswordHash(String text) throws HashException {
        try {
            salt = PasswordEncryptor.generateSixteenByteSalt();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
            outputStream.write(salt);

            byte[] concatenated = outputStream.toByteArray();

            passwordBytes = PasswordHasher.computeSHA512HashWithSalt(concatenated);
        } catch (IOException e) {
            throw new HashException("error hashing text");
        }
    }

    public byte[] getPasswordBytes() {
        return passwordBytes;
    }

    public byte[] getSalt() {
        return salt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PasswordHash that = (PasswordHash) o;
        return Arrays.equals(passwordBytes, that.passwordBytes) && Arrays.equals(salt, that.salt);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(passwordBytes);
        result = 31 * result + Arrays.hashCode(salt);
        return result;
    }
}
