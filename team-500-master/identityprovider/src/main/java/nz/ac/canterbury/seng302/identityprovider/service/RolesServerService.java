package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.ModifyRoleOfUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRoleChangeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles adding and removing roles to/from users, as according to the
 * user_accounts.proto file.
 */
@Service
public class RolesServerService {

  @Autowired private UserRepository repository;

  // Helper function to reduce duplicate code.
  // Gets the user, checks if modification is legal, modifies, saves.
  // Returns a UserRoleChangeResponse with the details of success or failure.
  // If addingRole is true, role will be added. If false, role will be deleted.
  private UserRoleChangeResponse modifyRoleOfUser(
      ModifyRoleOfUserRequest modificationRequest, boolean addingRole) {

    UserRole roleToChange = modificationRequest.getRole();
    var response = UserRoleChangeResponse.newBuilder();
    UserModel user = repository.findById(modificationRequest.getUserId());

    if (user == null) {
      return response.setIsSuccess(false).setMessage("Error: User does not exist.").build();
    }
    // If the role is being added, the user should not have the role.
    // If the role is being deleted, the user should have the role.
    if (addingRole != user.getRoles().contains(roleToChange)) {
      if (addingRole) {
        user.addRole(roleToChange);
      } else {
        if (user.getRoles().size() == 1) {
          return response
              .setIsSuccess(false)
              .setMessage("Error: User must have at least 1 role.")
              .build();
        }
        user.deleteRole(roleToChange);
      }
      repository.save(user);
      return response
          .setIsSuccess(true)
          .setMessage("Successfully " + (addingRole ? "added" : "removed") + " user role")
          .build();
    }
    String message =
        addingRole
            ? "Failed to add role, user already has role"
            : "Failed to remove role, user doesn't have role";
    return response.setIsSuccess(false).setMessage(message).build();
  }

  /**
   * Adds a role to the user if the user does not already have the role.
   *
   * @param modificationRequest The modification request for the role
   * @return UserRoleChangeResponse The status of how successful the modification was
   */
  public UserRoleChangeResponse addRoleToUser(ModifyRoleOfUserRequest modificationRequest) {
    return modifyRoleOfUser(modificationRequest, true);
  }

  /**
   * Removes a role to the user if the user does not already have the role.
   *
   * @param modificationRequest The modification request for the role
   * @return UserRoleChangeResponse The status of how successful the modification was
   */
  public UserRoleChangeResponse removeRoleFromUser(ModifyRoleOfUserRequest modificationRequest) {
    return modifyRoleOfUser(modificationRequest, false);
  }
}
