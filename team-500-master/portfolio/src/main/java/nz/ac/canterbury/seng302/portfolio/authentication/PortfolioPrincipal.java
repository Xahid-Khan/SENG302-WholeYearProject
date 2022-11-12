package nz.ac.canterbury.seng302.portfolio.authentication;

import java.util.List;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import java.security.Principal;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;


/**
 * A thin wrapper around the AuthState gRPC class. This is necessary to ensure that STOMP websockets
 * connect successfully.
 *
 * <p>
 *   STOMP calls toString() to serialise the Principal and injects the result as the value of the
 *   <code>user-name</code> field in the CONNECTED response from the server.<br/>
 *   AuthState's toString() response introduces a syntax error in the CONNECTED message, causing
 *   clients never to connect.
 * </p>
 */
public record PortfolioPrincipal(AuthState authState) implements Principal {
    @Override
    public String getName() {
        return authState.getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean getIsAuthenticated() {
        return authState.getIsAuthenticated();
    }

    public List<ClaimDTO> getClaimsList() {
        return authState.getClaimsList();
    }
}
