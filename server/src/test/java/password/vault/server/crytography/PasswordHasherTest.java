package password.vault.server.crytography;

import org.junit.Test;
import password.vault.server.cryptography.PasswordEncryptor;
import password.vault.server.cryptography.PasswordHasher;
import password.vault.server.exceptions.HashException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PasswordHasherTest {
    @Test
    public void testMD5HashOnPassword() {
        String password = "test12341234";
        String expectedMD5Hash = "3a0fd1b2d199e5155cc73b6fc16eaaf4";

        String hashedPassword = PasswordHasher.computeHash(password, PasswordHasher.MD5_MESSAGE_DIGEST_INSTANCE);

        assertEquals("unexpected MD5 hash for string", expectedMD5Hash, hashedPassword);
    }

    @Test
    public void testSHA1HashOnPassword() {
        String password = "test12341234";
        String expectedMD5Hash = "805fc3b72b1d632259794a9d7620ea3c72aefbcb";

        String hashedPassword = PasswordHasher.computeHash(password, PasswordHasher.SHA1_MESSAGE_DIGEST_INSTANCE);

        assertEquals("unexpected SHA1 hash for string", expectedMD5Hash, hashedPassword);
    }

    @Test
    public void testSHA256HashOnPassword() {
        String password = "test12341234";
        String expectedMD5Hash = "247dfa6801d335380c31b584998ea9b48baab7c7fae706a12477598e29972dee";

        String hashedPassword = PasswordHasher.computeHash(password, PasswordHasher.SHA256_MESSAGE_DIGEST_INSTANCE);
        String hashedPassword2 = PasswordHasher.computeHash(password, PasswordHasher.SHA256_MESSAGE_DIGEST_INSTANCE);

        assertEquals("unexpected SHA256 hash for string", expectedMD5Hash, hashedPassword);
        assertEquals("unexpected SHA256 hash for string", expectedMD5Hash, hashedPassword2);
    }

    @Test
    public void testComputeSHA512HashWithSalt() throws HashException, IOException {
        String text = "superSecretText";
        byte[] salt = PasswordEncryptor.generateSalt();

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(textBytes);
        outputStream.write(salt);

        byte[] concatenated = outputStream.toByteArray();

        byte[] hashFirstTime = PasswordHasher.computeSHA512HashWithSalt(concatenated);
        byte[] hashSecondTime = PasswordHasher.computeSHA512HashWithSalt(concatenated);

        System.out.println(Arrays.toString(hashFirstTime));
        System.out.println(Arrays.toString(hashSecondTime));

        assertArrayEquals(hashFirstTime, hashSecondTime);
    }
}