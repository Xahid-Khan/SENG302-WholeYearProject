package nz.ac.canterbury.seng302.identityprovider.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EditUserTest {

  @Autowired private RegisterServerService registerServerService;

  @Autowired private EditUserService editUserService;

  @Autowired private UserRepository userRepository;

  // A basic request to be used for tests here
  private UserRegisterRequest request =
      UserRegisterRequest.newBuilder()
          .setUsername("Username")
          .setPassword("Password")
          .setFirstName("FirstName")
          .setMiddleName("Middle Names")
          .setLastName("LastName")
          .setNickname("Nickname")
          .setBio("Bio")
          .setPersonalPronouns("Pronoun1/Pronoun2")
          .setEmail("email@email.email")
          .build();

  @BeforeEach
  private void clearDatabase() {
    userRepository.deleteAll();
  }

  @Test
  public void editValidUser() throws NoSuchAlgorithmException, InvalidKeySpecException {
    registerServerService.register(request);
    int userID = userRepository.findByUsername("Username").getId();
    EditUserRequest newRequest =
        EditUserRequest.newBuilder()
            .setUserId(userID)
            .setFirstName("FirstName")
            .setMiddleName("Middle Names")
            .setLastName("NewLastName")
            .setNickname("Nickname")
            .setBio("Bio")
            .setPersonalPronouns("Pronoun1/Pronoun2")
            .setEmail("email@email.email")
            .build();
    var response = editUserService.editUser(newRequest);

    // Check it was successful
    assertTrue(response.getIsSuccess());
    // Ensure that the message is sent successfully
    assertEquals("Updated details for user", response.getMessage().split(":", 2)[0]);
    // Ensure only 1 user exists
    assertEquals(1, userRepository.count());
    // Ensure user exists
    assertNotNull(userRepository.findByUsername("Username"));

    assertEquals("NewLastName", userRepository.findByUsername("Username").getLastName());
  }

  @Test
  public void editNonExistentUser() {
    EditUserRequest newRequest =
        EditUserRequest.newBuilder()
            .setUserId(1)
            .setFirstName("FirstName")
            .setMiddleName("Middle Names")
            .setLastName("NewLastName")
            .setNickname("Nickname")
            .setBio("Bio")
            .setPersonalPronouns("Pronoun1/Pronoun2")
            .setEmail("email@email.email")
            .build();
    var response = editUserService.editUser(newRequest);

    // Check it was not successful
    assertFalse(response.getIsSuccess());

    assertEquals("Error: User not in database", response.getMessage());
  }
}
