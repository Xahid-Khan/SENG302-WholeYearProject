package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.model.contract.UserContract;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
  public UserContract toContract(UserResponse userResponse) {
    return new UserContract(
        userResponse.getId(),
        userResponse.getFirstName(),
        userResponse.getMiddleName(),
        userResponse.getLastName(),
        userResponse.getNickname(),
        userResponse.getUsername(),
        userResponse.getEmail(),
        userResponse.getPersonalPronouns(),
        userResponse.getBio(),
        userResponse.getRolesList());
  }

  /**
   * This method gets a userResponse object and returns a user DTO object for use with Thymeleaf.
   *
   * @param userResponse
   * @return User
   */
  public User userResponseToUserDto(UserResponse userResponse) {
    return new User(
        userResponse.getUsername(),
        "",
        userResponse.getFirstName(),
        userResponse.getMiddleName(),
        userResponse.getLastName(),
        userResponse.getNickname(),
        userResponse.getBio(),
        userResponse.getPersonalPronouns(),
        userResponse.getEmail(),
        userResponse.getCreated());
  }
}
