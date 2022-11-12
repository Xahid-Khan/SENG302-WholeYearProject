package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** This controller controls the portion of the groups page which is visible to the user. */
@Controller
public class GroupsPageController extends AuthenticatedController {

  @Value("${nz.ac.canterbury.seng302.portfolio.urlPathPrefix}")
  private String urlPathPrefix;

  @Autowired
  public GroupsPageController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * GET /groups fetches the groups page. The groups page shows all groups
   *
   * @return The groups html page
   */
  @GetMapping(value = "/groups")
  public String getGroups(Model model) {
    model.addAttribute("relativePath", urlPathPrefix);
    return "groups";
  }
}
