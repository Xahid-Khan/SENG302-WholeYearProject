package nz.ac.canterbury.seng302.identityprovider.exceptions;

/**
 * This custom exception is used when the database's vendor is incorrect or unknown.
 */
public class DatabaseException extends RuntimeException {
  public DatabaseException(String errorMessage) {
    super(errorMessage);
  }

}
