package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.entity.SortingParameterEntity;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.RolesClientService;
import nz.ac.canterbury.seng302.portfolio.service.SortingParametersService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedGroupsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRoleChangeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller handles interactions with the 'View Users' page.
 */
@Controller
public class UserListController extends AuthenticatedController {
  private static final int PAGE_SIZE = 20;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  private RolesClientService rolesClientService;

  @Autowired
  private SortingParametersService sortingParametersService;

  @Autowired
  private GroupsClientService groupsClientService;

  @Autowired
  public UserListController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }


  /**
   * Loads the users when loading the user list page.
   *
   * @param principal the user's token
   * @param pageMaybe page (if provided)
   * @param sortAttributeMaybe sorting attribute (if provided)
   * @param ascendingMaybe sorting order (if provided)
   * @param model the model to send to the view
   * @return the user list page
   */
  @GetMapping("/user-list")
  public String listUsers(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestParam("page") Optional<Integer> pageMaybe,
      @RequestParam("sortBy") Optional<String> sortAttributeMaybe,
      @RequestParam("asc") Optional<String> ascendingMaybe,
      Model model
  ) {
    Optional<?>[] maybeVariables = {pageMaybe, sortAttributeMaybe, ascendingMaybe};
    // Standard for loop for mutability
    for (int i = 0; i < maybeVariables.length; i++) {
      if (maybeVariables[i].isPresent() && maybeVariables[i].get().toString().equals("undefined")) {
        maybeVariables[i] = Optional.empty();
      }
    }
    int userId = getUserId(principal);

    String sortAttributeString;
    // Ascending is true by default
    boolean ascending = true;
    // Since URL Params are always Strings, this check ensures that it's like a boolean.
    // If the value is false, the boolean is false, for any other value it will be true.
    if (ascendingMaybe.isPresent()) {
      ascending = ascendingMaybe.get().equals("true");
    }

    if (sortingParametersService.checkExistance(userId) && sortAttributeMaybe.isEmpty()) {
      SortingParameterEntity sortingParams = sortingParametersService.getSortingParams(userId);
      sortAttributeString = sortingParams.getSortAttribute();
      ascending = sortingParams.isSortOrder();

    } else if (sortAttributeMaybe.isPresent()) {
      sortAttributeString = sortAttributeMaybe.get();

      sortingParametersService.saveSortingParams(userId, sortAttributeString, ascending);
    } else {
      sortAttributeString = "name";
    }

    // Supply defaults
    int page = pageMaybe.orElse(1);

    // Validate inputs
    if (page < 1) {
      page = 1;
    }

    var sortAttribute = switch (sortAttributeString) {
      case "username" -> GetPaginatedUsersOrderingElement.USERNAME;
      case "alias" -> GetPaginatedUsersOrderingElement.NICKNAME;
      case "roles" -> GetPaginatedUsersOrderingElement.ROLES;
      default -> GetPaginatedUsersOrderingElement.NAME;
    };

    var offset = (page - 1) * PAGE_SIZE;

    // Make Request
    var response = userAccountService.getPaginatedUsers(
        offset,
        PAGE_SIZE,
        sortAttribute,
        ascending
    );
    // Construct response
    model.addAttribute("users", response.getUsersList());
    model.addAttribute(
        "totalUserCount",
        response.getPaginationResponseOptions().getResultSetSize()
    );
    model.addAttribute("pageOffset", offset);
    model.addAttribute("currentPage", page);
    model.addAttribute("sortDir", ascending);
    model.addAttribute("sortBy", sortAttributeString);
    model.addAttribute("delegate", this);
    model.addAttribute("pageSize", PAGE_SIZE);
    return "user_list";
  }



  /**
   * Helper function to assist with modifying roles on the user list page.
   *
   * @param principal the user's token
   * @param model the current model
   * @param id the ID of the user that is being changed
   * @param roleNumber the role's enum value
   * @param adding if the role is being added (true) or deleted (false)
   */
  private void modifyRole(PortfolioPrincipal principal, Model model, int id, Integer roleNumber,
      boolean adding) {
    int userId = getUserId(principal);
    model.addAttribute("roleMessageTarget", id);

    if (isTeacher(principal)) {
      if (userId != id) {
        UserRoleChangeResponse response = adding
            ? rolesClientService.addRole(id, UserRole.forNumber(roleNumber))
            : rolesClientService.removeRole(id, UserRole.forNumber(roleNumber));
        if (!response.getIsSuccess()) {
          model.addAttribute("roleMessage", response.getMessage());
        }
      } else {
        model.addAttribute("roleMessage", "Cannot modify roles for yourself");
      }
    } else {
      model.addAttribute("roleMessage", "Error: insufficient permissions");
    }
  }

  /**
   * Handles adding a role to a user.
   *
   * @param principal the user's token
   * @param model the current model
   * @param id the ID of the user that is being changed
   * @param roleNumber the role's enum value
   * @return the user list page (with updates)
   */
  @PostMapping("/user-list")
  public String addRole(@AuthenticationPrincipal PortfolioPrincipal principal,
      Model model,
      @RequestParam(name = "id") Integer id,
      @RequestParam(name = "roleNumber") Integer roleNumber) {
    modifyRole(principal, model, id, roleNumber, true);
    if (roleNumber == 1) {
      PaginatedGroupsResponse allGroupDetails = groupsClientService.getAllGroupDetails();
      boolean inNonMembersGroup = false;
      int nonGroupId = -1;
      for (GroupDetailsResponse group : allGroupDetails.getGroupsList()) {
        if (group.getShortName().equals("Non Group")) {
          nonGroupId = group.getGroupId();
          for (UserResponse user : group.getMembersList()) {
            if (user.getId() == id) {
              inNonMembersGroup = true;
              break;
            }
          }
        }
        if (group.getShortName().equals("Teachers")) {
          groupsClientService.addGroupMembers(group.getGroupId(), List.of(id));
        }
      }
      if (inNonMembersGroup) {
        groupsClientService.removeGroupMembers(nonGroupId, List.of(id));
      }
    }

    return listUsers(principal, Optional.empty(), Optional.empty(), Optional.empty(), model);
  }

  /**
   * Handles deleting a role from a user.
   *
   * @param principal the user's token
   * @param model the current model
   * @param id the ID of the user that is being changed
   * @param roleNumber the role's enum value
   * @return the user list page (with updates)
   */
  @DeleteMapping("/user-list")
  public String deleteRole(@AuthenticationPrincipal PortfolioPrincipal principal,
      Model model,
      @RequestParam(name = "id") Integer id,
      @RequestParam(name = "roleNumber") Integer roleNumber) {
    modifyRole(principal, model, id, roleNumber, false);

    if (roleNumber == 1) {
      PaginatedGroupsResponse allGroupDetails = groupsClientService.getAllGroupDetails();
      boolean inOtherGroup = false;
      int nonGroupId = -1;
      for (GroupDetailsResponse group : allGroupDetails.getGroupsList()) {
        if (group.getShortName().equals("Non Group")) {
          nonGroupId = group.getGroupId();
        }
        if (group.getShortName().equals("Teachers")) {
          groupsClientService.removeGroupMembers(group.getGroupId(), List.of(id));
        } else {
          for (UserResponse otherUser : group.getMembersList()) {
            if (otherUser.getId() == id) {
              inOtherGroup = true;
              break;
            }
          }
        }
      }
      if (!inOtherGroup) {
        groupsClientService.addGroupMembers(nonGroupId, List.of(id));
      }
    }

    return listUsers(principal, Optional.empty(), Optional.empty(), Optional.empty(), model);
  }

  /**
   * Formats the URL with the additional URL parameters.
   *
   * @param page the page to load
   * @param sortBy the sorting attribute
   * @param sortDir the sorting direction
   * @return a String with the format of the URL
   */
  public String formatUrl(int page, String sortBy, boolean sortDir) {
    return String.format("?page=%d&sortBy=%s&asc=%b", page, sortBy, sortDir);
  }

  /**
   * This function is used by Thymeleaf whenever a role must be displayed to the user.
   * It converts the role into a human-readable role, with correct capitalization.
   *
   * @param role the role to format
   * @return the human friendly readable output of the roles
   */
  public String formatUserRole(UserRole role) {
    return switch (role) {
      case STUDENT -> "Student";
      case TEACHER -> "Teacher";
      case COURSE_ADMINISTRATOR -> "Course Administrator";
      default -> "Role not found";
    };
  }

  /**
   * Gets the roles a user does NOT currently have.
   *
   * @param user the user to display what roles they don't have
   * @return the roles a user does NOT currently have
   */
  public List<UserRole> getAvailableRoles(UserResponse user) {
    List<UserRole> list = new ArrayList<>();
    for (UserRole role : UserRole.values()) {
      if (role != UserRole.UNRECOGNIZED && !user.getRolesList().contains(role)) {
        list.add(role);
      }
    }
    return list;
  }
}
