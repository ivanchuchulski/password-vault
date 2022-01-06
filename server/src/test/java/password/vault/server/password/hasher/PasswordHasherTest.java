package password.vault.server.password.hasher;

import org.junit.Test;

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

        assertEquals("unexpected SHA256 hash for string", expectedMD5Hash, hashedPassword);
    }
}