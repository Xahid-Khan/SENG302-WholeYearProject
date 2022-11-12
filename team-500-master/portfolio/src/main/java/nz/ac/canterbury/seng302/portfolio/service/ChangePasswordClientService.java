package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordClientService {

    @GrpcClient(value = "identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountsStub;

    public ChangePasswordResponse updatePassword(Integer userId, String currentPassword, String newPassword) {
        ChangePasswordRequest passwordRequest =
                ChangePasswordRequest.newBuilder()
                        .setUserId(userId)
                        .setCurrentPassword(currentPassword)
                        .setNewPassword(newPassword)
                        .build();
        return userAccountsStub.changeUserPassword(passwordRequest);
    }

}
