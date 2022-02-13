package password.vault.server.cryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/*
sources :
 https://www.baeldung.com/java-aes-encryption-decryption
 https://stackoverflow.com/a/9537017/9127495
 */
public class PasswordEncryptor {
    private static final byte[] KEY_SALT = {
            (byte) 0xc7, (byte) 0x73, (byte) 0x21, (byte) 0x8c,
            (byte) 0x7e, (byte) 0xc8, (byte) 0xee, (byte) 0x99
    };
    private static final String KEY_GENERATOR_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_ITERATIONS_COUNT = 4096; // or use 65536 or more for safer key
    private static final int KEY_LENGTH = 128;
    private static final String SECRET_KEY_SPEC_ALGORITHM = "AES";

    private static final String ENCRYPTION_ALGORITHM_NAME = "AES/CBC/PKCS5Padding";
    private static final IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);

    public static String encrypt(String input, SecretKey key)
            throws PasswordEncryptorException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);

            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

            byte[] cipherText = cipher.doFinal(input.getBytes());

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new PasswordEncryptorException("error : encrypting string", e);
        }
    }

    public static String decrypt(String cipherText, SecretKey key) throws PasswordEncryptorException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);

            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));

            return new String(plainText);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new PasswordEncryptorException("error : decrypting password", e);
        }
    }


    public static byte[] encrypt(String input, SecretKey key, byte[] ivBytes) throws PasswordEncryptorException {
        try {
            IvParameterSpec ivParameterSpec = PasswordEncryptor.generateIv(ivBytes);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            byte[] encodedAndEncrypted = Base64.getEncoder().encode(cipherText);

            return encodedAndEncrypted;
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new PasswordEncryptorException("error : encrypting string", e);
        }
    }

    public static String decrypt(byte[] cipherText, SecretKey key, byte[] ivBytes) throws
            PasswordEncryptorException {
        try {
            IvParameterSpec ivParameterSpec = PasswordEncryptor.generateIv(ivBytes);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] plainText = cipher.doFinal(decoded);

            return new String(plainText);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new PasswordEncryptorException("error : decrypting password", e);
        }
    }

    /* ----- */
    public static EncryptedPassword encryptWithMasterPassword(String password, String masterPassword) throws
            PasswordEncryptorException {
        try {
            byte[] salt = PasswordEncryptor.generateSixteenByteSalt();
            byte[] ivBytes = PasswordEncryptor.generateSixteenByteSalt();
            IvParameterSpec ivParameterSpec = PasswordEncryptor.generateIv(ivBytes);

            SecretKey key = getKeyFromString(masterPassword, salt);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);

            byte[] cipherText = cipher.doFinal(password.getBytes());
            byte[] encodedAndEncrypted = Base64.getEncoder().encode(cipherText);

            return new EncryptedPassword(encodedAndEncrypted, salt, ivBytes);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException e) {
            throw new PasswordEncryptorException("error : encrypting string", e);
        }
    }

    public static String decryptWithMasterPassword(EncryptedPassword encryptedPassword, String masterPassword) throws
            PasswordEncryptorException {
        try {
            SecretKey key = getKeyFromString(masterPassword, encryptedPassword.salt());

            IvParameterSpec ivParameterSpec = generateIv(encryptedPassword.iv());
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM_NAME);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedPassword.encryptedPassword());
            byte[] plainText = cipher.doFinal(decoded);

            return new String(plainText);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new PasswordEncryptorException("error : decrypting password", e);
        }
    }

    public static SecretKey getKeyFromString(String str) throws PasswordEncryptorException {
        return getKeyFromString(str, KEY_SALT);
    }

    public static SecretKey getKeyFromString(String str, byte[] salt) throws PasswordEncryptorException {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);

            KeySpec keySpec = new PBEKeySpec(str.toCharArray(), salt, KEY_ITERATIONS_COUNT, KEY_LENGTH);

            return new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), SECRET_KEY_SPEC_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PasswordEncryptorException("error : generating secret key", e);
        }
    }

    public static IvParameterSpec generateIv(byte[] iv) {
        return new IvParameterSpec(iv);
    }

    public static byte[] generateSixteenByteSalt() {
        byte[] salt = new byte[16];
        // SecureRandom sha1PRGNG = SecureRandom.getInstance("SHA1PRGNG");
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public static class PasswordEncryptorException extends Exception {
        public PasswordEncryptorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
