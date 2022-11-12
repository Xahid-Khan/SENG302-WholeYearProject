package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserAccountServiceTest {
  @Autowired private PasswordService passwordService;

  @Autowired private UserRepository repository;

  @BeforeEach
  public void setup() throws NoSuchAlgorithmException, InvalidKeySpecException {
    repository.deleteAll();

    var user1 =
        new UserModel(
            "u1",
            passwordService.hashPassword("pass"),
            "c",
            "a",
            "a",
            "Y",
            "u1bio",
            "they/them",
            "u1@example.com",
            List.of(UserRole.STUDENT, UserRole.TEACHER));
    var user2 =
        new UserModel(
            "u2",
            passwordService.hashPassword("pass"),
            "b",
            "b",
            "b",
            "Z",
            "u2bio",
            "he/him",
            "u2@example.com",
            List.of(UserRole.STUDENT, UserRole.COURSE_ADMINISTRATOR));
    var user3 =
        new UserModel(
            "u3",
            passwordService.hashPassword("pass"),
            "a",
            "c",
            "c",
            "X",
            "u3bio",
            "she/her",
            "u3@example.com",
            List.of(UserRole.TEACHER));

    repository.saveAll(Arrays.stream(new UserModel[] {user1, user2, user3}).toList());
  }
}
