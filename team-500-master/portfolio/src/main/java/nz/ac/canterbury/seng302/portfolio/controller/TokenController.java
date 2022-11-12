package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles the request to validate the token.
 */
@Controller
@RequestMapping("/api/v1")
public class TokenController extends AuthenticatedController {

  /**
   * This is similar to autowiring, but apparently recommended more than field injection.
   *
   * @param authStateService   an AuthStateService
   * @param userAccountService a UserAccountService
   */
  protected TokenController(
      AuthStateService authStateService,
      UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * Validates the token.
   *
   * @param principal the user's token
   * @return HTTP 200 if token is in sync, HTTP 401 if not
   */
  @GetMapping(value = "validate_token")
  public ResponseEntity<?> validateToken(@AuthenticationPrincipal PortfolioPrincipal principal) {
    if (tokenInSync(principal)) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
