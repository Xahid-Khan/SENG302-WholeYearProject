package nz.ac.canterbury.seng302.portfolio.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRoleChangeResponse;
import org.springframework.stereotype.Service;

/** This service will take the Authentication Principal and look for roles in the token. */
@Service
public class RolesClientService {

  @GrpcClient("identity-provider-grpc-server")
  private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountServiceBlockingStub;

  /**
   * Handles obtaining the roles from the token. It will parse them into their correct UserRole
   * equivalents such that they can be handled as enum values.
   * TODO upon teaching team replying:
   * Once obtaining is completed, it will
   * then check against the IDP to ensure the roles are correct, and if they are not then it will
   * update the token to match the database.
   *
   * @param principal the PortfolioPrincipal to get the roles from
   * @return a list of UserRoles containing whatever is in the token
   */
  public List<UserRole> getRolesByToken(PortfolioPrincipal principal) {
    // Get roles as String array. I.E.: "STUDENT", or "STUDENT, TEACHER"
    String roles =
        principal.getClaimsList().stream()
            .filter(claim -> claim.getType().equals("role"))
            .findFirst()
            .map(ClaimDTO::getValue)
            .orElse(UserRole.UNRECOGNIZED.toString());

    // TODO: Token update/second half of JavaDoc:
    // There are two cases in which we need to change the token. One is if the lists have changed
    // size in comparison to each other, and the other is if the roles in the lists are different.
    // A HashSet here is used for performance reasons.
    //if (!(databaseRoles.size() == tokenRoles.size()
        //&& new HashSet<>(databaseRoles).containsAll(tokenRoles))) {
    //}
    // Maps each role to UserRole enum if possible.
    return roles.contains(" ")
        ? Arrays.stream(roles.split(", ")).map(String::toUpperCase).map(UserRole::valueOf).toList()
        : Arrays.stream(roles.split(",")).map(String::toUpperCase).map(UserRole::valueOf).toList();
  }

  /**
   * Removes a role from the user by connecting to the RolesServerService over gRPC.
   *
   * @param id the ID of the user whose roles need to change
   * @param role the role to remove from the user
   * @return a gRPC ModifyRoleOfUserResponse
   */
  public UserRoleChangeResponse removeRole(int id, UserRole role) {
    ModifyRoleOfUserRequest req = ModifyRoleOfUserRequest.newBuilder()
        .setUserId(id)
        .setRole(role)
        .build();
    return userAccountServiceBlockingStub.removeRoleFromUser(req);
  }

  /**
   * Adds a role to the user by connecting to the RolesServerService over gRPC.
   *
   * @param id the ID of the user whose roles need to change
   * @param role the role to add to the user
   * @return a gRPC ModifyRoleOfUserResponse
   */
  public UserRoleChangeResponse addRole(int id, UserRole role) {
    ModifyRoleOfUserRequest req = ModifyRoleOfUserRequest.newBuilder()
        .setUserId(id)
        .setRole(role)
        .build();
    return userAccountServiceBlockingStub.addRoleToUser(req);
  }
}
