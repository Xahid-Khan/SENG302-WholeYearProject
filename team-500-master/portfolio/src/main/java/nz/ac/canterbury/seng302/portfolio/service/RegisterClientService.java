package nz.ac.canterbury.seng302.portfolio.service;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ProfilePhotoUploadMetadata;
import nz.ac.canterbury.seng302.shared.identityprovider.UploadUserProfilePhotoRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import org.springframework.stereotype.Service;

/** Registers a new client by passing off the details to the RegisterServerService. */
@Service
public class RegisterClientService {

  @GrpcClient(value = "identity-provider-grpc-server")
  private UserAccountServiceGrpc.UserAccountServiceBlockingStub registrationStub;

  @GrpcClient(value = "identity-provider-grpc-server")
  private UserAccountServiceGrpc.UserAccountServiceStub nonBlockingStub;

  /**
   * Registers a new user.
   *
   * @param user The user to register
   * @return a UserRegisterResponse for the status of the registration
   */
  public UserRegisterResponse register(User user) {
    UserRegisterRequest regRequest =
        UserRegisterRequest.newBuilder()
            .setUsername(user.username())
            .setPassword(user.password())
            .setFirstName(user.firstName())
            .setMiddleName(user.middleName())
            .setLastName(user.lastName())
            .setNickname(user.nickname())
            .setBio(user.bio())
            .setPersonalPronouns(user.personalPronouns())
            .setEmail(user.email())
            .build();
    return registrationStub.register(regRequest);
  }

  public EditUserResponse updateDetails(User user, Integer userId) {
    EditUserRequest editRequest =
        EditUserRequest.newBuilder()
            .setUserId(userId)
            .setFirstName(user.firstName())
            .setMiddleName(user.middleName())
            .setLastName(user.lastName())
            .setNickname(user.nickname())
            .setBio(user.bio())
            .setPersonalPronouns(user.personalPronouns())
            .setEmail(user.email())
            .build();
    return registrationStub.editUser(editRequest);
  }

  /**
   * This method uses a bidirectional gRPC service the upload the image data to the database and get
   * the status response back. We Override gRPCs' UploadUserProfilePhotoRequest and pass it
   * FileUploadStatusResponse that will provide us with Upload status (Success, Pending, etc.)
   *
   * @param userId id (integer) of the user who is uploading the photo
   * @param fileType extension of the file (JPG, PNG, etc)
   * @param uploadImage Raw image data in byte array
   */
  public void uploadUserPhoto(int userId, String fileType, byte[] uploadImage) {
    CountDownLatch latch = new CountDownLatch(1);
    ByteString imageBuffer = ByteString.copyFrom(uploadImage);
    var requestStreamObserverContainer =
        new Object() {
          StreamObserver<UploadUserProfilePhotoRequest> observer;
        };

    var responseStreamObserver =
        new StreamObserver<FileUploadStatusResponse>() {
          int nextStartIndex = 0;

          @Override
          public void onNext(FileUploadStatusResponse response) {
            try {
              if (response.getStatus().equals(FileUploadStatus.PENDING)
                  || response.getStatus().equals(FileUploadStatus.IN_PROGRESS)) {
                var endIndex = Math.min(imageBuffer.size() - 1, nextStartIndex + (1024 * 1024));

                if (endIndex == nextStartIndex) {
                  requestStreamObserverContainer.observer.onCompleted();
                  return;
                }

                var chunk = imageBuffer.substring(nextStartIndex, endIndex);
                nextStartIndex = endIndex;

                UploadUserProfilePhotoRequest imageDataRequest =
                    UploadUserProfilePhotoRequest.newBuilder().setFileContent(chunk).build();
                requestStreamObserverContainer.observer.onNext(imageDataRequest);
              } else if (response.getStatus().equals(FileUploadStatus.SUCCESS)) {
                latch.countDown();
                onCompleted();
              }
            } catch (Exception e) {
              onError(e);
            }
          }

          @Override
          public void onError(Throwable t) {
            latch.countDown();
            t.printStackTrace();
          }

          @Override
          public void onCompleted() {
            latch.countDown();
          }
        };

    requestStreamObserverContainer.observer =
        nonBlockingStub.uploadUserProfilePhoto(responseStreamObserver);

    var metadataRequest =
        UploadUserProfilePhotoRequest.newBuilder()
            .setMetaData(
                ProfilePhotoUploadMetadata.newBuilder().setUserId(userId).setFileType(fileType))
            .build();

    requestStreamObserverContainer.observer.onNext(metadataRequest);
  }

  public void deleteUserPhoto(int userId) {
    DeleteUserProfilePhotoRequest.Builder request = DeleteUserProfilePhotoRequest.newBuilder();
    request.setUserId(userId);

    registrationStub.deleteUserProfilePhoto(request.build());
  }
}
