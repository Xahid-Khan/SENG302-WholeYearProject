package nz.ac.canterbury.seng302.identityprovider.mapping;

import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides utility methods for mapping between different representations of a User.
 */
@Component
public class UserMapper {

  @Autowired
  private TimestampMapper timestampMapper;

  /**
   * Map a UserModel from the database to a UserResponse for sending to clients;
   *
   * @param user UserModel to map
   * @return UserResponse representing the given user
   */
  public UserResponse toUserResponse(UserModel user) {
    return UserResponse.newBuilder()
        .setId(user.getId())
        .setUsername(user.getUsername())
        .setFirstName(user.getFirstName())
        .setMiddleName(user.getMiddleName())
        .setLastName(user.getLastName())
        .setNickname(user.getNickname())
        .setBio(user.getBio())
        .setPersonalPronouns(user.getPersonalPronouns())
        .setEmail(user.getEmail())
        .setCreated(timestampMapper.toProtobufTimestamp(user.getCreated()))
        .addAllRoles(user.getRoles())
        .build();
  }
}
