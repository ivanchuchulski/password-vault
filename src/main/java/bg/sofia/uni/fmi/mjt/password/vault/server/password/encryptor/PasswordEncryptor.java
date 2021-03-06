package bg.sofia.uni.fmi.mjt.password.vault.server.password.encryptor;

import bg.sofia.uni.fmi.mjt.password.vault.server.exceptions.password.PasswordEncryptorException;

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

    public static String decrypt(String cipherText, SecretKey key)
            throws PasswordEncryptorException {
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

    public static SecretKey getKeyFromString(String password) throws PasswordEncryptorException {
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);

            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), KEY_SALT, KEY_ITERATIONS_COUNT, KEY_LENGTH);

            return new SecretKeySpec(secretKeyFactory.generateSecret(keySpec).getEncoded(), SECRET_KEY_SPEC_ALGORITHM);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new PasswordEncryptorException("error : generating secret key", e);
        }
    }

}
