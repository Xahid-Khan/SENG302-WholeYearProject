package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * A controller to manage the request for group post page.
 */

@Controller
public class GroupFeedPageController extends AuthenticatedController {

  @Value("${nz.ac.canterbury.seng302.portfolio.urlPathPrefix}")
  private String urlPathPrefix;

  @Autowired
  private GroupsClientService groupsClientService;

  /**
   * This is similar to autowiring, but apparently recommended more than field injection.
   *
   * @param authStateService   an AuthStateService
   * @param userAccountService a UserAccountService
   */
  protected GroupFeedPageController(
      AuthStateService authStateService,
      UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  @GetMapping(value = "/group_feed/{groupId}", produces = "application/json")
  public String getGroupFeed(@PathVariable Integer groupId, Model model,
      @AuthenticationPrincipal PortfolioPrincipal principal) {
    model.addAttribute("isMember",
        groupsClientService.isMemberOfTheGroup(getUserId(principal), groupId));
    model.addAttribute("relativePath", urlPathPrefix);
    return "group_feed";
  }
}
