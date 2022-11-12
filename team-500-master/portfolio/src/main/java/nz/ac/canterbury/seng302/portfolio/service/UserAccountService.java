package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.shared.identityprovider.GetPaginatedUsersRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.springframework.stereotype.Service;

/**
 * A service that manages CRUD operations for View Account Controller.
 */

@Service
public class UserAccountService {

  @GrpcClient("identity-provider-grpc-server")
  private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountServiceBlockingStub;

  /**
  * This service is used by controller to request the details of a given user
  * @param userId Id of a user
  * @return details of user or null in case of no user found.
  */
  public UserResponse getUserById(int userId) {
    GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder()
        .setId(userId)
        .build();
    var user = userAccountServiceBlockingStub.getUserAccountById(userRequest);
    if (user.getUsername().length() > 0) {
        return user;
    } else {
        return null;
    }
  }

  /**
  * Retrieve a window into a list of all users, sorted into a given order.
  *
  * @param offset number of users to skip before opening the window
  * @param limit maximum number of users to include in the window
  * @param orderBy parameter of a user to order the list by
  * @return list of users within the window and the total number of users available
  */
  public PaginatedUsersResponse getPaginatedUsers(int offset, int limit, GetPaginatedUsersOrderingElement orderBy, boolean ascending) {
    var orderByAttributeName = switch (orderBy) {
      case NAME -> "name";
      case NICKNAME -> "nickname";
      case USERNAME -> "username";
      case ROLES -> "roles";
    };

    GetPaginatedUsersRequest allUsers = GetPaginatedUsersRequest.newBuilder().setPaginationRequestOptions(
        PaginationRequestOptions.newBuilder()
            .setOffset(offset)
            .setLimit(limit)
            .setOrderBy(String.format("%s|%s", orderByAttributeName, ascending ? "asc" : "desc"))
            .build()).build();

    return userAccountServiceBlockingStub.getPaginatedUsers(allUsers);
  }
}