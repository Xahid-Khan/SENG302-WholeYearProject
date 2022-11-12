package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Handles the GET request on the /monthly-planner endpoint. */
@Controller
public class MonthlyPlannerController extends AuthenticatedController {

  @Value("${nz.ac.canterbury.seng302.portfolio.urlPathPrefix}")
  private String urlPathPrefix;

  @Autowired
  public MonthlyPlannerController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * GET /monthly-planner/{projectId} fetches monthly planner view for a specific project projectId,
   * this page shows a calendar view with all sprints showing from the related project.
   *
   * @param model the model to add the relative path to
   * @return The monthly_planner html page
   */
  @GetMapping("/monthly-planner/{projectId}")
  public String getMonthlyPlanner(@AuthenticationPrincipal PortfolioPrincipal principal, Model model) {
    model.addAttribute("relativePath", urlPathPrefix);
    model.addAttribute("canEdit", isTeacher(principal));
    return "monthly_planner";
  }
}
