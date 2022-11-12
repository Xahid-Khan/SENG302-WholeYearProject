package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@SpringBootTest
public class UserCreatedTimestampTest {

  @Autowired private UserAccountService userAccountService;

  @Autowired private UserRepository repository;

  private StreamObserver<UserRegisterResponse> observer = mock(StreamObserver.class);

  private StreamObserver<UserResponse> viewObserver = mock(StreamObserver.class);

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
  public void clearDatabase() {
    repository.deleteAll();
  }
  /**
   * Tests registering a completely valid user. Credit to https://stackoverflow.com/a/49872463 for
   * providing how to run a Mockito mock observer.
   */
  @Test
  public void registerUserCheckCreatedTimestamp() {
    Timestamp roughTime = currentTimestamp();
    userAccountService.register(request, observer);

    // Ensure request was only run once
    Mockito.verify(observer, times(1)).onCompleted();
    // Set up a captor for the response
    ArgumentCaptor<UserRegisterResponse> captor =
        ArgumentCaptor.forClass(UserRegisterResponse.class);
    // Capture the response
    Mockito.verify(observer, times(1)).onNext(captor.capture());
    // Get the UserRegisterResponse from the captor
    UserRegisterResponse response = captor.getValue();
    // Check it was successful
    assertTrue(response.getIsSuccess());

    GetUserByIdRequest req = GetUserByIdRequest.newBuilder().setId(response.getNewUserId()).build();
    userAccountService.getUserAccountById(req, viewObserver);

    // Ensure request was only run once
    Mockito.verify(viewObserver, times(1)).onCompleted();
    // Set up a captor for the response
    ArgumentCaptor<UserResponse> captor2 = ArgumentCaptor.forClass(UserResponse.class);
    // Capture the response
    Mockito.verify(viewObserver, times(1)).onNext(captor2.capture());
    // Get the UserRegisterResponse from the captor
    UserResponse res = captor2.getValue();

    // Check created at the right time (current time) +- 5 seconds
    assertTrue(Math.abs(res.getCreated().getSeconds() - roughTime.getSeconds()) < 5);
  }

  /**
   * Helper function to get the current timestamp.
   *
   * @return the current timestamp
   */
  private static Timestamp currentTimestamp() {
    return Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build();
  }
}
