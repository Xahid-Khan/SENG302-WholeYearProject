package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.portfolio.service.SubscriptionService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This class tests the Registration Controller, which is used for handling reasonable inputs on
 * what a user can register as. Note: All Null test cases are not included here as they break the
 * tests, however manual testing shows that Nulls are fine. To retest Nulls, either intercept with
 * BurpSuite and submit a blank post request, or use this cURL command: curl -X POST -d ""
 * http://localhost:9000/register
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private RegisterClientService service;

  @MockBean private AuthenticateClientService authService;

  @MockBean
  private GroupsClientService groupsClientService;

  @MockBean
  private SubscriptionService subscriptionService;

  @BeforeEach
  public void beforeEach() {
    GroupDetailsResponse group = GroupDetailsResponse.newBuilder()
        .setGroupId(1)
        .setShortName("test")
        .setLongName("Long Test")
        .addMembers(UserResponse.newBuilder()
            .setId(-1)
            .build())
        .build();
    Mockito.when(groupsClientService.getGroupById(any())).thenReturn(group);
    when(authService.authenticate(any(), any()))
        .thenReturn(
            AuthenticateResponse.newBuilder()
                .setUserId(0)
                .setToken("test_token")
                .setUsername("test_user")
                .setFirstName("Test")
                .setLastName("User")
                .setEmail("test@example.com")
                .setSuccess(true)
                .setMessage("Login successful")
                .build());
  }

  private final String API_PATH = "/register";

  // Helper function to place create a Post Body out of a user.
  // Note that a factory or builder pattern could've been used here,
  //  however this is just for testing.
  private String buildPostBody(User user) {
    return "username="
        + user.username()
        + "&email="
        + user.email()
        + "&password="
        + user.password()
        + "&firstName="
        + user.firstName()
        + "&middleName="
        + user.middleName()
        + "&lastName="
        + user.lastName()
        + "&nickname="
        + user.nickname()
        + "&personalPronouns="
        + user.personalPronouns()
        + "&bio="
        + user.bio()
        + "&register=0";
  }

  // Helper function to submit a registration to a mock /register endpoint when posting valid user.
  private MvcResult submitValidRegistration(User user) throws Exception {
    // Creates the Post Body to be sent to the mock /register endpoint
    var postBody = buildPostBody(user);
    // Creates a mock for the /register endpoint
    when(service.register(any()))
        .thenReturn(
            UserRegisterResponse.newBuilder()
                .setIsSuccess(true)
                .setNewUserId(1)
                .setMessage("Mock executed successfully")
                .build());

    // Performs the request to the /register endpoint
    return this.mockMvc
        .perform(
            post(API_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(postBody)
                .header("Host", "localhost:8080"))
        .andExpect(status().is3xxRedirection())
        .andReturn();
  }

  // Helper function to submit a registration to a mock /register endpoint when posting invalid
  // user.
  private MvcResult submitInvalidRegistration(User user) throws Exception {
    // Creates the Post Body to be sent to the mock /register endpoint
    var postBody = buildPostBody(user);
    // Creates a mock for the /register endpoint
    when(service.register(any()))
        .thenReturn(
            UserRegisterResponse.newBuilder()
                .setIsSuccess(true)
                .setNewUserId(1)
                .setMessage("Mock executed successfully")
                .build());

    // Performs the request to the /register endpoint
    return this.mockMvc
        .perform(
            post(API_PATH).contentType(MediaType.APPLICATION_FORM_URLENCODED).content(postBody))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  // Helper function for extrapolating if the request was invalid.
  private boolean wasError(MvcResult result) throws Exception {
    // If there was an error, then form-error should appear at least once.
    return result.getResponse().getContentAsString().contains("form-error");
  }

  /**
   * A simple test to ensure that the Thymeleaf template is not broken for the registration form.
   *
   * @throws Exception if perform fails for some reason
   */
  @Test
  void getRegistrationForm() throws Exception {
    // If Thymeleaf throws an exception, it will be caught via this test.
    this.mockMvc.perform(get(API_PATH)).andExpect(status().isOk());
  }

  /**
   * Registers a valid user.
   *
   * @throws Exception if perform fails for some reason
   */
  @Test
  void registerValidUser() throws Exception {
    var validUser =
        new User(
            "Username1",
            "Password1",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(validUser);

    assertFalse(wasError(result));
  }

  /**
   * Tests (in order): Empty String, String too short (2 characters), String too long (33
   * characters)
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @ValueSource(
      strings = {"", "AA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "Test!", "Test !", "Test$%"})
  void registerInvalidUsernames(String username) throws Exception {
    var user =
        new User(
            username,
            "Password1",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitInvalidRegistration(user);

    assertTrue(wasError(result));
  }

  /**
   * Tests (in order): Min string length (3), Max string length (32)
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @ValueSource(strings = {"AAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"})
  void registerBoundaryUsernames(String username) throws Exception {
    var user =
        new User(
            username,
            "Password1",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Ẽzra", "1IfTest", "Jōhn", "李二二", "张二二"})
  void registerValidEdgeUsernames(String username) throws Exception {
    var user =
        new User(
            username,
            "Password1",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  /**
   * Tests (in order): Empty String, String too short (7 characters)
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @ValueSource(strings = {"", "AAAAAAA"})
  void registerInvalidPasswords(String password) throws Exception {
    var user =
        new User(
            "Username",
            password,
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitInvalidRegistration(user);

    assertTrue(wasError(result));
  }

  /**
   * Tests a password with 8 characters
   *
   * @throws Exception if perform fails for some reason
   */
  @Test
  void registerBoundaryPassword() throws Exception {
    var user =
        new User(
            "Username",
            "AAAAAAAA",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  /**
   * Tests (in order): <br>
   * Empty first name, valid middle name(s), valid last name <br>
   * Valid first name, valid middle name(s), empty last name <br>
   * Invalid first name (too long), valid middle name(s), valid last name <br>
   * Valid first name, invalid middle name(s) (too long), valid last name <br>
   * Valid first name, valid middle name(s), invalid last name (too long) <br>
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @CsvSource({
    "'',Middle Names,LastName",
    "FirstName,Middle Names,''",
    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,Middle Names,LastName",
    "FirstName,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,LastName",
    "FirstName,Middle Names,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
  })
  void registerInvalidNames(String firstName, String middleNames, String lastName)
      throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            firstName,
            middleNames,
            lastName,
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitInvalidRegistration(user);

    assertTrue(wasError(result));
  }

  @ParameterizedTest
  @CsvSource({
    "'Cody',Middle Names,LastName",
    "John,Middle Names,'Smith-Jackson'",
    "Mary-Jane,Middle Names,LastName",
    "Smíth,Middle,Smith/Watson"
  })
  void registerValidEdgeNames(String firstName, String middleNames, String lastName)
      throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            firstName,
            middleNames,
            lastName,
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  /**
   * Tests (in order): <br>
   * Size 50 first name, valid middle name(s), valid last name <br>
   * Valid first name, Size 50 middle name(s), valid last name <br>
   * Valid first name, valid middle name(s), Size 50 last name <br>
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @CsvSource({
    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,Middle Names,LastName",
    "FirstName,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,LastName",
    "FirstName,Middle Names,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
  })
  void registerBoundaryNames(String firstName, String middleNames, String lastName)
      throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            firstName,
            middleNames,
            lastName,
            "Nickname",
            "Bio",
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  /**
   * Tests (in order): <br>
   * Invalid nickname (too long), valid bio, valid personalPronouns <br>
   * Valid nickname, invalid bio (too long), valid personalPronouns <br>
   * Valid nickname, valid bio, invalid personalPronouns (too long) <br>
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @CsvSource({
    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,bio,He/Him",
    "nickname,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,He/Him",
    "nickname,bio,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
  })
  void registerInvalidAdditionalInfo(String nickname, String bio, String personalPronouns)
      throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            "firstName",
            "middle Names",
            "lastName",
            nickname,
            bio,
            personalPronouns,
            "email%40email.com",
            currentTimestamp());

    var result = submitInvalidRegistration(user);

    assertTrue(wasError(result));
  }

  /**
   * Tests (in order): <br>
   * Empty nickname, valid bio, valid personalPronouns <br>
   * Valid nickname, empty bio, valid personalPronouns <br>
   * Valid nickname, valid bio, empty personalPronouns <br>
   * Size 32 nickname, valid bio, valid personalPronouns <br>
   * Valid nickname, size 512 bio, valid personalPronouns <br>
   * Valid nickname, valid bio, size 50 personalPronouns <br>
   * TODO: Since pronouns have changed, this should be replaced with more thorough checking of TODO:
   * pronouns
   *
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @CsvSource({
    "'',bio,He/Him",
    "nickname,'',He/Him",
    "nickname,bio,''",
    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,bio,He/Him",
    "nickname,AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,He/Him",
    "nickname,bio,He/Him",
  })
  void registerBoundaryAdditionalInfo(String nickname, String bio, String personalPronouns)
      throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            "firstName",
            "middle Names",
            "lastName",
            nickname,
            bio,
            personalPronouns,
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(user);

    assertFalse(wasError(result));
  }

  /**
   * Tests a blank email. Unfortunately email is quite hard to test, so we have to assume that JavaX
   * has the correct regex for it. The valid email test falls under the valid user test.
   */
  @Test
  void registerEmptyEmail() throws Exception {
    var user =
        new User(
            "Username",
            "Password",
            "firstName",
            "middle Names",
            "lastName",
            "nickname",
            "bio",
            "He/Him",
            "",
            currentTimestamp());

    var result = submitInvalidRegistration(user);

    assertTrue(wasError(result));
  }

  /**
   * Tests using valid bios. In order, tests characters and the valid special characters.
   *
   * @param bio Bio input
   * @throws Exception if perform fails for some reason
   */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "Test",
        "%21%3F.%2C%23%24%25%5E%26%2A%28%29%5B%5D%7B%7D%3B%27%3C%3E%3A%22-%3D_%2B",
        "\uD83D\uDE97",
        "\uD83D\uDE02",
        "♪",
        "🐱‍👤"
      })
  void registerValidBio(String bio) throws Exception {
    var validUser =
        new User(
            "Username",
            "Password",
            "FirstName",
            "Middle Names",
            "LastName",
            "Nickname",
            bio,
            "He/Him",
            "email%40email.com",
            currentTimestamp());

    var result = submitValidRegistration(validUser);

    assertFalse(wasError(result));
  }

  /**
   * Creates the current timestamp
   *
   * @return timestamp
   */
  public static Timestamp currentTimestamp() {
    return Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build();
  }
}
