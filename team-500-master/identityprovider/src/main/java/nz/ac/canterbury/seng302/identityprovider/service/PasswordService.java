package nz.ac.canterbury.seng302.identityprovider.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

/**
 * Service that provides a clean interface for securely hashing and verifying passwords.
 *
 * <p>
 *   Currently the
 * </p>
 *
 * <p>
 *   Based on this article: https://nullbeans.com/hashing-passwords-in-spring-applications/#Hashing_passwords
 * </p>
 */
@Service
public class PasswordService {
  public static int saltLength = 16;
  public static int iterationCount = 96;
  public static int keyLength = 512;

  /**
   * Securely hash a password string into a string that can be safely stored in the database.
   *
   * <p>
   *   In fact, the string returned actually contains more than just the plain hash.
   *   It contains three pieces of data, seperated by the pipe symbol: version, salt, and the hash.
   * </p>
   *
   * <p>
   *   This allows all necessary information to be stored in a single database column while
   *   conserving the metadata needed to verify the password later.
   * </p>
   *
   * @param password plaintext password to hash.
   * @return hashed version of the given password (including the salt attached in plaintext)
   */
  public String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    var secureRandomGenerator = new SecureRandom();
    var salt = secureRandomGenerator.generateSeed(saltLength);

    var hash = hashPassword(password, salt);
    return String.format(
        "1|%s|%s",
        Base64.getEncoder().encodeToString(salt),
        Base64.getEncoder().encodeToString(hash)
    );
  }

  /**
   * Securely hash a password string using a salt.
   *
   * @param password to hash
   * @param salt to use in the hash
   * @return byte array of the hash
   */
  protected byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    var pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
    var keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

    return keyFactory.generateSecret(pbeKeySpec).getEncoded();
  }

  /**
   * Verify that the given password and the hash match.
   *
   * <p>
   *   Note that this method extracts metadata (such as the version and the salt) from the string.
   *   Please refer to {@link PasswordService#hashPassword(String)} for more information.
   * </p>
   *
   * @param password to check against the hash
   * @param hashString to check the password against
   */
  public boolean verifyPassword(String password, String hashString)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Split the hash into version, salt, and hash.
    var hashParts = hashString.split("\\|");
    if (hashParts.length != 3 || !hashParts[0].equals("1")) {
      // Unsupported hash format
      return false;
    }

    var salt = Base64.getDecoder().decode(hashParts[1]);
    var trueHash = Base64.getDecoder().decode(hashParts[2]);

    var passwordHash = hashPassword(password, salt);

    return Arrays.equals(trueHash, passwordHash);
  }
}
