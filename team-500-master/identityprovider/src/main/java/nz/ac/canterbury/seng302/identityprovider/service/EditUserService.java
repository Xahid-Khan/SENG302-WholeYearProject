package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.database.PhotoModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserPhotoRepository;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserResponse;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EditUserService {

  @Autowired private UserRepository repository;

  @Autowired private UserPhotoRepository photoRepository;

  public EditUserResponse editUser(EditUserRequest request) {
    EditUserResponse.Builder reply = EditUserResponse.newBuilder();
    UserModel existingUser = repository.findById(request.getUserId());
    if (existingUser == null) {
      reply
          .setIsSuccess(false)
          .setMessage("Error: User not in database")
          .addValidationErrors(
              ValidationError.newBuilder()
                  .setFieldName("ID")
                  .setErrorText("Error: User not in database"));
    } else {
      UserModel newUser =
          new UserModel(
              existingUser.getUsername(),
              existingUser.getPasswordHash(),
              request.getFirstName(),
              request.getMiddleName(),
              request.getLastName(),
              request.getNickname(),
              request.getBio(),
              request.getPersonalPronouns(),
              request.getEmail(),
              existingUser.getRoles());
      newUser.setId(request.getUserId());
      newUser.setTimestamp(existingUser.getCreated());
      repository.save(newUser);
      reply.setIsSuccess(true).setMessage("Updated details for user: " + newUser);
    }

    return reply.build();
  }

  /**
   * This method saves the raw image data into the database in ByteString format.
   *
   * @param userId id (integer) of the user
   * @param rawImageData Image that needs to be saved to DB
   * @return a FileUploadStatusResponse
   */
  public FileUploadStatusResponse uploadUserPhoto(int userId, byte[] rawImageData) {
    FileUploadStatusResponse.Builder reply = FileUploadStatusResponse.newBuilder();
    try {
      PhotoModel newPhoto = new PhotoModel(userId, rawImageData);
      photoRepository.save(newPhoto);
      reply.setStatus(FileUploadStatus.SUCCESS);
    } catch (Exception e) {
      reply.setStatus(FileUploadStatus.FAILED).setMessage(e.getMessage());
    }
    return reply.build();
  }

  /**
   * this method overrides the current profile photo in the database for the given user ID with
   * null.
   *
   * @param request id (integer) of the user.
   * @return an updated DeleteUserProfilePhotoResponse
   */
  public DeleteUserProfilePhotoResponse deletePhoto(DeleteUserProfilePhotoRequest request) {
    DeleteUserProfilePhotoResponse.Builder reply = DeleteUserProfilePhotoResponse.newBuilder();
    try {
      photoRepository.deleteById(request.getUserId());
      reply.setIsSuccess(true);
      reply.setMessage("User profile photo has been deleted");
    } catch (Exception e) {
      reply.setIsSuccess(false);
      reply.setMessage("Unable to delete user profile photo");
    }
    return reply.build();
  }
}
