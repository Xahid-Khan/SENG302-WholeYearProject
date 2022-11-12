package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeFeedController {
    @Autowired
    private AuthStateService authStateService;

    @Autowired
    private UserAccountService userAccountService;

    @GetMapping(value = "/home_feed", produces = "application/json")
    public String getHomeFeed(@AuthenticationPrincipal PortfolioPrincipal principal, Model model) {
        Integer userId = authStateService.getId(principal);

        UserResponse user = userAccountService.getUserById(userId);
        model.addAttribute("userId", userId);
        model.addAttribute("username", user.getUsername());

        return "home_page";
    }
}
