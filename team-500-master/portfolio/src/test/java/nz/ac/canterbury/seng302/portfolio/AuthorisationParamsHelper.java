package nz.ac.canterbury.seng302.portfolio;

import java.util.List;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * This class handles functionality regarding authorization parameters, primarily used in testing.
 * It can build a claim around given UserRoles, and can build a claim for one or more UserRole.
 */
public class AuthorisationParamsHelper {

  /**
   * Helper function to build the claim together and configure correctly.
   *
   * @param name the type/name of the claim
   * @param roles the roles to add to the claim
   */
  private static void buildClaim(String name, String roles) {
    ClaimDTO.Builder newClaim = ClaimDTO.newBuilder();
    newClaim.setIssuer("Local Auths");
    newClaim.setType(name);
    newClaim.setValue(roles);

    AuthState.Builder newState = AuthState.newBuilder();
    newState
        .addClaims(newClaim)
        .setIsAuthenticated(true)
        .setNameClaimType("name")
        .setRoleClaimType("role")
        .setAuthenticationType("AuthenticationTypes.Federation");

    // With complements to: https://stackoverflow.com/a/46631015
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    SecurityContextHolder.setContext(securityContext);

    AuthState authState = newState.build();
    PortfolioPrincipal principal = new PortfolioPrincipal(authState);
    Mockito.when(authentication.getPrincipal()).thenReturn(principal);
  }

  /**
   * Creates and configures a token.
   *
   * @param name the name/type of the claim
   * @param roles a list of UserRoles to add to the claim
   */
  public static void setParams(String name, List<UserRole> roles) {
    buildClaim(name, roles.stream().map(Object::toString).collect(Collectors.joining(", ")));
  }

  /**
   * Creates and configures a token.
   *
   * @param name the name/type of the claim
   * @param role the role for the claim to have
   */
  public static void setParams(String name, UserRole role) {
    buildClaim(name, role.toString());
  }
}
