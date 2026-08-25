package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Simple SHA-256 + per-user salt password hashing.
 * Not bank-grade (that's out of scope for this project - see spec section 32),
 * but it means plain-text passwords are never stored or compared.
 */
public class PasswordUtil {

    private PasswordUtil() { }

    /** Returns "salt:hash", both Base64-encoded, ready to store in one DB column. */
    public static String hash(String plainPassword) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            byte[] hash = sha256(plainPassword, salt);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    /** Verifies a plain password against a stored "salt:hash" string. */
    public static boolean verify(String plainPassword, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        try {
            String[] parts = stored.split(":", 2);
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = sha256(plainPassword, salt);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] sha256(String plainPassword, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        return digest.digest(plainPassword.getBytes());
    }
}
