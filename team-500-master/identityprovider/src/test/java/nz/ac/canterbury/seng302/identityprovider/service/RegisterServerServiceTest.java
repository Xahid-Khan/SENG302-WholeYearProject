package nz.ac.canterbury.seng302.identityprovider.service;

import static org.junit.jupiter.api.Assertions.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RegisterServerServiceTest {

  @Autowired private RegisterServerService registerServerService;

  @Autowired private UserRepository userRepository;

  // A basic request to be used for tests here
  private UserRegisterRequest.Builder request =
      UserRegisterRequest.newBuilder()
          .setUsername("Username")
          .setPassword("Password")
          .setFirstName("FirstName")
          .setMiddleName("Middle Names")
          .setLastName("LastName")
          .setNickname("Nickname")
          .setBio("Bio")
          .setPersonalPronouns("Pronoun1/Pronoun2")
          .setEmail("email@email.email");

  @BeforeEach
  private void clearDatabase() {
    userRepository.deleteAll();
  }

  /** Tests registering a completely valid user. */
  @Test
  public void registerValidUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Register the valid request to ensure there is data in the database.
    var response = registerServerService.register(request.build());

    // Ensures registration was a success
    assertTrue(response.getIsSuccess());
    // Ensure that the message is sent successfully
    assertEquals("Registered new user", response.getMessage().split(":", 2)[0]);
    // Ensure only 1 user exists
    assertEquals(1, userRepository.count());
    // Ensure user exists
    assertNotNull(userRepository.findByUsername("Username"));
  }

  /** Submits two users, the second one having the same username as the first. */
  @Test
  public void registerDuplicateUsername() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Register the valid request to ensure there is data in the database.
    registerServerService.register(request.build());
    // Sets the email differently for the second user,
    //  such that a duplicate email error will not be raised
    request.setEmail("notthesame@email.com");

    var response = registerServerService.register(request.build());

    // Ensure only 1 user exists
    assertEquals(1, userRepository.count());
    // Ensure it failed
    assertFalse(response.getIsSuccess());
    // Ensure that the message is sent successfully
    assertTrue(response.getValidationErrors(0).getErrorText().contains("Username already in use"));
  }

  /**
   * Attempts to register a user with an email already in use, which should cause an email error.
   */
  @Test
  public void registerDuplicateEmail() throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Register the valid request to ensure there is data in the database.
    registerServerService.register(request.build());
    // Sets the username differently for the second user,
    //  such that a duplicate username error will not be raised
    request.setUsername("NotTheSameUsername");
    var response = registerServerService.register(request.build());

    // Ensure only 1 user exists
    assertEquals(1, userRepository.count());
    // Ensure it failed
    assertFalse(response.getIsSuccess());
    // Ensure that the message is sent successfully
    assertTrue(response.getValidationErrors(0).getErrorText().contains("Email already in use"));
  }

  /**
   * Attempts to register a user with both a username and email already in use.
   */
  @Test
  public void registerDuplicateUsernameAndDuplicateEmail()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    // Register the valid request to ensure there is data in the database.
    registerServerService.register(request.build());

    // Registers the same user, which should raise both errors.
    var response = registerServerService.register(request.build());
    // Ensure only 1 user exists
    assertEquals(1, userRepository.count());
    // Ensure it failed
    assertFalse(response.getIsSuccess());
    // Ensures two validation error messages were sent
    assertEquals(2, response.getValidationErrorsList().size());
  }
}
