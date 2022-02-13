package password.vault.server.crytography;

import org.junit.Test;
import password.vault.server.cryptography.EncryptedPassword;
import password.vault.server.cryptography.PasswordEncryptor;

import javax.crypto.SecretKey;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class PasswordEncryptorTest {
    @Test
    public void testEncryptingAStringAndDecryptingItReturnsTheSameString() throws
            PasswordEncryptor.PasswordEncryptorException {
        String inputString = "testPassword1234";
        SecretKey key = PasswordEncryptor.getKeyFromString(inputString);

        String encryptedString = PasswordEncryptor.encrypt(inputString, key);
        String decryptedString = PasswordEncryptor.decrypt(encryptedString, key);

        assertEquals(inputString, decryptedString);
    }

    @Test
    public void testEncryptingAStringAndDecryptingItReturnsTheSameString2() throws
            PasswordEncryptor.PasswordEncryptorException {
        String input = "_rrR~S>k$[8+Ps/x2WyaFv";
        SecretKey key = PasswordEncryptor.getKeyFromString(input);

        String encryptedString = PasswordEncryptor.encrypt(input, key);
        String decryptedString = PasswordEncryptor.decrypt(encryptedString, key);

        assertEquals(input, decryptedString);
    }

    @Test
    public void testEncryptingAStringAndDecryptingItReturnsTheSameString3() throws
            PasswordEncryptor.PasswordEncryptorException {
        String input = "pass";

        byte[] salt = PasswordEncryptor.generateSixteenByteSalt();
        byte[] ivBytes = PasswordEncryptor.generateSixteenByteSalt();

        // the key can be derived differently
        SecretKey key = PasswordEncryptor.getKeyFromString(input + Arrays.toString(salt));

        byte[] encryptedString = PasswordEncryptor.encrypt(input, key, ivBytes);
        String decryptedString = PasswordEncryptor.decrypt(encryptedString, key, ivBytes);

        System.out.println(Arrays.toString(encryptedString));

        assertEquals(input, decryptedString);
    }

    @Test
    public void testEncryptionWithMasterPassword() throws PasswordEncryptor.PasswordEncryptorException {
        String password = "pass1234";
        String masterPass = "themasterPass";

        EncryptedPassword encryptedPassword = PasswordEncryptor.encryptWithMasterPassword(password, masterPass);
        String decryptedPassword = PasswordEncryptor.decryptWithMasterPassword(encryptedPassword, masterPass);

        assertEquals("decrypted password should match the original one", password, decryptedPassword);
    }
}