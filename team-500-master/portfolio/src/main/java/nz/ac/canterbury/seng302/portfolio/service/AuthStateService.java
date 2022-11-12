package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.springframework.stereotype.Service;

/** A Service that takes in AuthState objects and returns fields from inside them. */
@Service
public class AuthStateService {
  /**
   * The user's id from this AuthState.
   *
   * @return The user's id from this AuthState
   */
  public Integer getId(PortfolioPrincipal principal) {
    return Integer.valueOf(getClaimByType(principal.authState(), "nameid", "-100"));
  }

  /**
   * The user's role from this AuthState.
   *
   * @return The user's role from this AuthState
   */
  public String getRole(AuthState authState) {
    return getClaimByType(authState, "role", "NOT FOUND");
  }

  private String getClaimByType(AuthState authState, String claimType, String defaultValue) {
    return authState.getClaimsList().stream()
        .filter(claim -> claim.getType().equals(claimType))
        .findFirst()
        .map(ClaimDTO::getValue)
        .orElse(defaultValue);
  }
}
