package password.vault.server.cryptography;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class PasswordHash {
    private final byte[] passwordBytes;
    private final byte[] salt;

    public PasswordHash(String text) throws PasswordHasher.HashException {
        this(text, PasswordEncryptor.generateRandomSixteenBytes());
    }

    public PasswordHash(String text, byte[] salt) throws PasswordHasher.HashException {
        try {
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            this.salt = salt;

            byte[] concatenated = concatenatePasswordAndSalt(salt, textBytes);

            this.passwordBytes = PasswordHasher.computeSHA512HashWithSalt(concatenated);
        } catch (IOException e) {
            throw new PasswordHasher.HashException("error building hash", e);
        }
    }

    public PasswordHash(byte[] passwordBytes, byte[] salt) {
        this.passwordBytes = passwordBytes;
        this.salt = salt;
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


    private byte[] concatenatePasswordAndSalt(byte[] salt, byte[] passwordBytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(passwordBytes);
        outputStream.write(salt);

        return outputStream.toByteArray();
    }

}
