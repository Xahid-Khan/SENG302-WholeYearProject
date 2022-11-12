package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRoleChangeResponse;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This base service contains multiple other services, and is used as a hub so GRPC does not
 * complain about multiple services extending UserAccountServiceGrpc.UserAccountServiceImplBase.
 */
@GrpcService
public class UserAccountService extends UserAccountServiceGrpc.UserAccountServiceImplBase {
  @Autowired private RegisterServerService registerServerService;

  @Autowired private GetUserService getUserService;

  @Autowired private RolesServerService rolesServerService;

  @Autowired private EditUserService editUserService;

  @Autowired private ChangePasswordService changePasswordService;

  /**
   * This is a GRPC user service method that is being over-ridden to register a user and return a
   * UserRegisterRequest
   *
   * @param request parameters from the caller
   * @param responseObserver to receive results or errors
   */
  @Override
  public void register(
      UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
    try {
      var response = registerServerService.register(request);

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  /**
   * This is a GRPC user service method that is being over-ridden to edit the users details and
   * return * a EditUserResponse
   *
   * @param request parameters from the caller
   * @param responseObserver to receive results or errors
   */
  @Override
  public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
    try {
      var response = editUserService.editUser(request);

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  /**
   * This is a GRPC user service method that is being over-ridden to get the user details and encase
   * them into a User Response body. if the user is not found the User response is set to null
   *
   * @param request parameters from the caller
   * @param responseObserver to receive results or errors
   */
  @Override
  public void getUserAccountById(
      GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
    try {
      var res = getUserService.getUserAccountById(request);

      responseObserver.onNext(res);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  /**
   * GRPC service method that provides a list of user details with a caller-supplied sort order,
   * maximum length, and offset.
   *
   * @param request parameters from the caller
   * @param responseObserver to receive results or errors
   */
  @Override
  public void getPaginatedUsers(
      GetPaginatedUsersRequest request, StreamObserver<PaginatedUsersResponse> responseObserver) {
    try {
      var response = getUserService.getPaginatedUsers(request);

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  /**
   * Adds a role to the user if the user does not already have the role.
   *
   * @param modificationRequest the modification request for the role
   * @param responseObserver to receive results or errors
   */
  @Override
  public void addRoleToUser(
      ModifyRoleOfUserRequest modificationRequest,
      StreamObserver<UserRoleChangeResponse> responseObserver) {

    var response = rolesServerService.addRoleToUser(modificationRequest);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  /**
   * Removes a role to the user if the user does not already have the role.
   *
   * @param modificationRequest The modification request for the role
   * @param responseObserver to receive results or errors
   */
  @Override
  public void removeRoleFromUser(
      ModifyRoleOfUserRequest modificationRequest,
      StreamObserver<UserRoleChangeResponse> responseObserver) {
    var response = rolesServerService.removeRoleFromUser(modificationRequest);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  /**
   * Uploads the stream of data from User and sends back the status response based on the upload
   * status.
   *
   * @param responseObserver is of FileUploadStatusResponse
   * @return a UploadUserProfilePhotoRequest
   */
  @Override
  public StreamObserver<UploadUserProfilePhotoRequest> uploadUserProfilePhoto(
      StreamObserver<FileUploadStatusResponse> responseObserver) {
    return new StreamObserver<UploadUserProfilePhotoRequest>() {
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      Integer userId = null;
      String fileType = null;

      @Override
      public void onNext(UploadUserProfilePhotoRequest request) {
        FileUploadStatusResponse.Builder reply = FileUploadStatusResponse.newBuilder();

        if (request.hasMetaData()) {
          userId = request.getMetaData().getUserId();
          fileType = request.getMetaData().getFileType();

          reply.setStatus(FileUploadStatus.PENDING);
          reply.setMessage("Pending");
        } else {
          if (userId == null) {
            responseObserver.onError(new Throwable("Image chunk must be sent after metadata."));
            return;
          }

          ByteString chunk = request.getFileContent();
          try {
            buffer.write(chunk.toByteArray());
          } catch (IOException e) {
            responseObserver.onError(
                new Throwable("Exception occurred while loading image chunk."));
            return;
          }

          reply.setStatus(FileUploadStatus.IN_PROGRESS);
          reply.setMessage("Chunk received.");
        }

        responseObserver.onNext(reply.build());
      }

      @Override
      public void onError(Throwable t) {
      }

      @Override
      public void onCompleted() {
        if (buffer.size() == 0) {
          responseObserver.onError(new Throwable("Image data must be transferred."));
          return;
        }

        FileUploadStatusResponse uploadStatus =
            editUserService.uploadUserPhoto(userId, buffer.toByteArray());

        if (uploadStatus.getStatus() == FileUploadStatus.FAILED) {
          responseObserver.onError(new Throwable(String.valueOf(uploadStatus.getStatus())));
          return;
        }

        FileUploadStatusResponse.Builder reply = FileUploadStatusResponse.newBuilder();
        reply.setStatus(uploadStatus.getStatus()).setMessage(uploadStatus.getMessage());
        responseObserver.onNext(reply.build());
      }
    };
  }

  /**
   * Overriding a gRPC service to delete a photo from the DB and generate a response stream to send
   * back to user
   *
   * @param request DeleteUserProfilePhotoRequest form user with the user Id as an element
   * @param responseObserver returns a response observer stream with status.
   */
  @Override
  public void deleteUserProfilePhoto(
      DeleteUserProfilePhotoRequest request,
      StreamObserver<DeleteUserProfilePhotoResponse> responseObserver) {
    try {
      DeleteUserProfilePhotoResponse response = editUserService.deletePhoto(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Exception e) {
      responseObserver.onError(e);
    }
  }

  @Override
  public void changeUserPassword(
      ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
    try {
      var response = changePasswordService.changePassword(request);

      responseObserver.onNext(response);
      responseObserver.onCompleted();

    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }
}
