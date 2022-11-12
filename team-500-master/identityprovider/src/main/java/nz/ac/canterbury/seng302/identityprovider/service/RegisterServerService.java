package nz.ac.canterbury.seng302.identityprovider.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberModel;
import nz.ac.canterbury.seng302.identityprovider.database.GroupMemberRepository;
import nz.ac.canterbury.seng302.identityprovider.database.GroupModel;
import nz.ac.canterbury.seng302.identityprovider.database.GroupRepository;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles the server side registration of a user.
 */
@Service
public class RegisterServerService {

  @Autowired
  private UserRepository repository;

  @Autowired
  private PasswordService passwordService;

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private GroupMemberRepository groupMemberRepository;

  /**
   * Registers a new user, including hashing their password and saving the current timestamp.
   *
   * @param request the UserRegisterRequest to parse
   * @return a UserRegisterResponse reply containing either a success or failure, and validation
   * errors in the case of a failure
   * @throws NoSuchAlgorithmException if password hashing fails
   * @throws InvalidKeySpecException  if password hashing fails
   */
  public UserRegisterResponse register(UserRegisterRequest request)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();
    var passwordHash = passwordService.hashPassword(request.getPassword());
    List<UserRole> roles = new ArrayList<>();
    roles.add(UserRole.STUDENT);
    UserModel user =
        new UserModel(
            request.getUsername(),
            passwordHash,
            request.getFirstName(),
            request.getMiddleName(),
            request.getLastName(),
            request.getNickname(),
            request.getBio(),
            request.getPersonalPronouns(),
            request.getEmail(),
            roles);

    List<ValidationError> validationErrors = new ArrayList<>();

    // If a username already exists in the database, add a validation error to the response.
    if (repository.findByUsername(request.getUsername()) != null) {
      validationErrors.add(
          ValidationError.newBuilder()
              .setFieldName("username")
              .setErrorText("Error: Username already in use")
              .build());
      reply.setMessage("Error: Username already in use");
    }

    if (repository.findByEmail(request.getEmail()) != null) {
      validationErrors.add(
          ValidationError.newBuilder()
              .setFieldName("email")
              .setErrorText("Error: Email already in use")
              .build());
      reply.setMessage("Error: Email already in use");
    }

    if (validationErrors.isEmpty()) {
      repository.save(user);
      UserModel newUser = repository.findByUsername(request.getUsername());
      try {
        GroupModel nonGroup = groupRepository.findByShortName("Non Group");
        GroupMemberModel groupMember = groupMemberRepository.findById(nonGroup.getId()).get();
        groupMember.addUserIds(Collections.singletonList(newUser.getId()));
        groupMemberRepository.save(groupMember);
      } catch (NullPointerException e) {
        e.getMessage();
      }
      reply
          .setIsSuccess(true)
          .setNewUserId(user.getId())
          .setMessage("Registered new user: " + user);
    } else {
      reply.setIsSuccess(false).setNewUserId(-1);
      if (validationErrors.size() == 2) {
        reply.setMessage("Error: Username and email already in use");
      }

      for (ValidationError validationError : validationErrors) {
        reply.addValidationErrors(validationError);
      }
    }

    return reply.build();
  }
}
