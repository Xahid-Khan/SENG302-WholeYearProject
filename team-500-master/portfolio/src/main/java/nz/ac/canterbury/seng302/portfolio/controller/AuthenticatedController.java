package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.List;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.RolesClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * This controller always adds user information to any page which requires authorization to see.
 * This allows any details about the user to be rendered or accessed (based on their current token).
 */
@Controller
public abstract class AuthenticatedController {
  private final AuthStateService authStateService;

  private final UserAccountService userAccountService;

  @Autowired private RolesClientService rolesClientService;

  /**
   * This is similar to autowiring, but apparently recommended more than field injection.
   *
   * @param authStateService an AuthStateService
   * @param userAccountService a UserAccountService
   */
  protected AuthenticatedController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    this.authStateService = authStateService;
    this.userAccountService = userAccountService;
  }

  /**
   * Loads the user model into the view based on their token.
   *
   * @param principal the user's token
   * @return a UserResponse object with all the user details
   */
  @ModelAttribute("user")
  public UserResponse getUser(@AuthenticationPrincipal PortfolioPrincipal principal) {
    int userId = getUserId(principal);

    return userAccountService.getUserById(userId);
  }

  /**
   * Loads the user's ID into the view based on the token. The reason this is separate to the user
   * is in the instance that a user wants to view another user, the user model attribute will need
   * to be overridden. As such, this method provides a way to consistently ensure the users ACTUAL
   * ID is also loaded for the navbar for instance.
   *
   * @param principal the user's token
   * @return the user's ID
   */
  @ModelAttribute("userId")
  public int getUserId(@AuthenticationPrincipal PortfolioPrincipal principal) {
    return authStateService.getId(principal);
  }

  /**
   * Loads the user's username into the view based on the token. The reason this is separate to the
   * user is in the instance that a user wants to view another user, the user model attribute will
   * need to be overridden. As such, this method provides a way to consistently ensure the users
   * ACTUAL username is also loaded for the navbar for instance.
   *
   * @param principal the user's token
   * @return the user's username
   */
  @ModelAttribute("username")
  private String getUserName(@AuthenticationPrincipal PortfolioPrincipal principal) {
    return getUser(principal).getUsername();
  }

  /**
   * This function helps controllers get the user's roles. If there is a mismatch in roles between
   * the database and the token, then the token will be updated here such that the user's roles are
   * up-to-date.
   *
   * @param principal the user's token
   * @return the user's roles as a list
   */
  private List<UserRole> getRoles(PortfolioPrincipal principal) {
    return rolesClientService.getRolesByToken(principal);
  }

  /**
   * Returns the highest role that a user has. I.E., (STUDENT, TEACHER) returns TEACHER, etc. This
   * is according to the UserRole enum. This is pulled from the IDP database to ensure it is always
   * up-to-date.
   *
   * <p>Note that this probably shouldn't be accessed from controllers for authentication, as
   * isTeacher or isCourseAdmin handle effectively the same request whilst making the code look
   * nicer.
   *
   * @param principal the user's token
   * @return the user's highest role
   */
  private UserRole getHighestRole(PortfolioPrincipal principal) {
    UserRole currentHighestRole = UserRole.STUDENT;
    List<UserRole> roles = getUser(principal).getRolesList();
    for (UserRole role : roles) {
      if (role.compareTo(currentHighestRole) > 0) {
        currentHighestRole = role;
      }
    }
    return currentHighestRole;
  }

  /**
   * Registers when the roles in the token are out of sync.
   *
   * @param principal the user's token
   * @return if the token is in sync or not
   */
  @ModelAttribute("tokenInSync")
  public boolean tokenInSync(@AuthenticationPrincipal PortfolioPrincipal principal) {
    return getUser(principal).getRolesList().equals(getRoles(principal));
  }

  /**
   * Runs a compareTo to check if the highest role is higher than a student. If it is, the user must
   * have all the permissions of a teacher.
   *
   * @param principal the user's token
   * @return if the user is a teacher or above or not
   */
  @ModelAttribute("isTeacher")
  public boolean isTeacher(@AuthenticationPrincipal PortfolioPrincipal principal) {
    return getHighestRole(principal).compareTo(UserRole.STUDENT) > 0;
  }

  /**
   * Runs a compareTo to check if the highest role is higher than a teacher. If it is, the user must
   * have all the permissions of a course administrator.
   *
   * @param principal the user's token
   * @return if the user is a course administrator or not
   */
  @ModelAttribute("isCourseAdmin")
  public boolean isCourseAdmin(@AuthenticationPrincipal PortfolioPrincipal principal) {
    return getHighestRole(principal).compareTo(UserRole.TEACHER) > 0;
  }
}
