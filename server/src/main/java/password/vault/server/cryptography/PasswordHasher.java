package password.vault.server.cryptography;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 source :
 https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
*/
public class PasswordHasher {
    public static final String MD5_MESSAGE_DIGEST_INSTANCE = "MD5";
    public static final String SHA1_MESSAGE_DIGEST_INSTANCE = "SHA-1";
    public static final String SHA256_MESSAGE_DIGEST_INSTANCE = "SHA-256";
    public static final String SHA512_MESSAGE_DIGEST_INSTANCE = "SHA-512";

    public static String computeHash(String password, String messageDigestInstance) throws
            HashException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(messageDigestInstance);

            return hashString(password, messageDigest);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new HashException(String.format("error : no such message digest algorithm found %s ",
                                                  messageDigestInstance), noSuchAlgorithmException);
        }
    }

    public static byte[] computeSHA512HashWithSalt(byte[] text) throws HashException {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA512_MESSAGE_DIGEST_INSTANCE);
            return messageDigest.digest(text);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new HashException("error : no message digest algorithm found for sha-512", noSuchAlgorithmException);
        }
    }

    private static String hashString(String passwordToHash, MessageDigest messageDigest) {
        //Get the hash's bytes
        byte[] bytes = messageDigest.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder stringBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            stringBuilder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return stringBuilder.toString();
    }

    public static class HashException extends Exception {
        public HashException(String message) {
            super(message);
        }

        public HashException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
