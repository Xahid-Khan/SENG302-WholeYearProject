package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.portfolio.DTO.Login;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** This controller handles logging the user in. */
@Controller
public class LoginController {

  @Autowired private AuthenticateClientService authenticateClientService;

  /**
   * GET /login provides the user with a GUI form. This form sends a POST request to /login
   * containing the information from the form, running the login process.
   *
   * @param error If there is an error as a parameter within the URL, it will be rendered in the
   *     HTML
   * @param model Parameters sent to thymeleaf template to be rendered into HTML
   * @return The login_form.html page
   */
  @GetMapping(value = "/login")
  public String login(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestParam(name = "error", required = false) String error,
      Model model) {
    if (principal != null && principal.getIsAuthenticated()) {
      return "redirect:my_account";
    }

    model.addAttribute("login", new Login());
    model.addAttribute("error", error);
    return "login_form";
  }

  /**
   * Attempts to authenticate with the Identity Provider via gRPC.
   *
   * <p>This process works in a few stages: 1. The login credentials are received in the body of a
   * POST request 2. We send the username and password to the IdP 3. We check the response, and if
   * it is successful we add a cookie to the HTTP response so that the client's browser will store
   * it to be used for future authentication with this service. 4. If there is an error, 4a.
   * Otherwise, 4b 4a. The user will be redirected to the /login endpoint with 'error' parameters
   * appended to the URL 4b. The user will be redirected to the /greeting page
   *
   * @param request HTTP request sent to this endpoint
   * @param response HTTP response that will be returned by this endpoint
   * @param login The credentials sent by the form
   * @param redirectAttributes Used for sending parameters with the redirections
   * @return Redirection depending on flow of login
   */
  @PostMapping("/login")
  public String login(
      HttpServletRequest request,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes,
      Login login,
      Model model) {
    if (login.getUsername() == null || login.getPassword() == null) {
      model.addAttribute("error", "Invalid form data provided");
      return "login_form";
    }

    AuthenticateResponse loginReply;
    try {
      loginReply = authenticateClientService.authenticate(login.getUsername(), login.getPassword());
    } catch (StatusRuntimeException e) {
      model.addAttribute("error", "Error connecting to Identity Provider...");
      return "login_form";
    }

    if (loginReply.getSuccess()) {
      var domain = request.getHeader("host");
      CookieUtil.create(
          response,
          "lens-session-token",
          loginReply.getToken(),
          !domain.startsWith("localhost"),
          5 * 60 * 60, // Expires in 5 hours
          domain.startsWith("localhost") ? null : domain);
      // Redirect user if login succeeds
      redirectAttributes.addFlashAttribute("message", "Successfully logged in.");
      model.addAttribute("userId", loginReply.getUserId());
      model.addAttribute("username", loginReply.getUsername());
      return "redirect:my_account";
    }

    model.addAttribute("error", loginReply.getMessage());
    return "login_form";
  }

  /** Allows get requests to / to redirect to the login form. */
  @GetMapping("/")
  public String index(@AuthenticationPrincipal PortfolioPrincipal principal) {
    if (principal != null) {
      return "redirect:my_account";
    } else {
      return "redirect:login";
    }
  }
}
