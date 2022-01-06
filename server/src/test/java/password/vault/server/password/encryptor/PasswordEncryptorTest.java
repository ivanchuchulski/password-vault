package password.vault.server.password.encryptor;

import org.junit.Test;
import password.vault.server.exceptions.password.PasswordEncryptorException;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;

public class PasswordEncryptorTest {
    @Test
    public void testEncryptingAStringAndDecryptingItReturnsTheSameString() throws PasswordEncryptorException {
        String inputString = "testPassword1234";
        SecretKey key = PasswordEncryptor.getKeyFromString(inputString);

        String encryptedString = PasswordEncryptor.encrypt(inputString, key);
        String decryptedString = PasswordEncryptor.decrypt(encryptedString, key);

        assertEquals(inputString, decryptedString);
    }

    @Test
    public void testEncryptingAStringAndDecryptingItReturnsTheSameString2() throws PasswordEncryptorException {
        String input = "_rrR~S>k$[8+Ps/x2WyaFv";
        SecretKey key = PasswordEncryptor.getKeyFromString(input);

        String encryptedString = PasswordEncryptor.encrypt(input, key);
        String decryptedString = PasswordEncryptor.decrypt(encryptedString, key);

        assertEquals(input, decryptedString);
    }
}