package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.RolesClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/** Handles loading the project details page. */
@Controller
public class ProjectDetailsController extends AuthenticatedController {

  @Value("${nz.ac.canterbury.seng302.portfolio.urlPathPrefix}")
  private String urlPathPrefix;

  @Autowired
  private RolesClientService rolesService;

  @Autowired
  private AuthStateService authStateService;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  public ProjectDetailsController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * Loads the project details page.
   *
   * @param model the model to load the relative path into
   * @return the project details page
   */
  @GetMapping("/project-details")
  public String projectDetails(@AuthenticationPrincipal PortfolioPrincipal principal, Model model) {
    List<UserRole> roles = rolesService.getRolesByToken(principal);

    model.addAttribute("isStudent", roles.size() == 1 && roles.contains(UserRole.STUDENT));
    model.addAttribute("isCourseAdmin", roles.contains(UserRole.COURSE_ADMINISTRATOR));

    Integer userId = authStateService.getId(principal);

    UserResponse userDetails = userAccountService.getUserById(userId);

    model.addAttribute("userId", userId);
    model.addAttribute("username", userDetails.getUsername());

    model.addAttribute("relativePath", urlPathPrefix);
    return "project_details";
  }
}
