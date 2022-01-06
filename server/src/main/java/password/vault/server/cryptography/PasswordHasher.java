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

    public static String computeHash(String password, String messageDigestInstance) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(messageDigestInstance);

            return hashString(password, messageDigest);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new RuntimeException(String.format("error : no such message digest algorithm found %s ",
                                                     messageDigestInstance), noSuchAlgorithmException);
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
}
