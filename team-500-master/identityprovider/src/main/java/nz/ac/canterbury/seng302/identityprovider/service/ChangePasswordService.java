package nz.ac.canterbury.seng302.identityprovider.service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordService {

  @Autowired private UserRepository repository;

  @Autowired private PasswordService passwordService;

  public ChangePasswordResponse changePassword(ChangePasswordRequest request)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    UserModel userDetails = repository.findById(request.getUserId());
    boolean changeSuccess =
        passwordService.verifyPassword(request.getCurrentPassword(), userDetails.getPasswordHash());
    String message;
    if (changeSuccess) {
      try {
        UserModel newUser =
            new UserModel(
                userDetails.getUsername(),
                passwordService.hashPassword(request.getNewPassword()),
                userDetails.getFirstName(),
                userDetails.getMiddleName(),
                userDetails.getLastName(),
                userDetails.getNickname(),
                userDetails.getBio(),
                userDetails.getPersonalPronouns(),
                userDetails.getEmail(),
                userDetails.getRoles());
        newUser.setId(request.getUserId());
        newUser.setTimestamp(userDetails.getCreated());
        repository.save(newUser);
        message = "User password updated successfully";
      } catch (IllegalArgumentException e) {
        changeSuccess = false;
        message = "Unable to update user password";
      }
    } else {
      message = "Current password is incorrect";
    }

    return ChangePasswordResponse.newBuilder()
        .setIsSuccess(changeSuccess)
        .setMessage(message)
        .build();
  }
}
