package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.authentication.AuthenticationServerInterceptor;
import nz.ac.canterbury.seng302.identityprovider.authentication.JwtTokenUtil;
import nz.ac.canterbury.seng302.identityprovider.database.UserModel;
import nz.ac.canterbury.seng302.identityprovider.database.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticationServiceGrpc.AuthenticationServiceImplBase;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.stream.Collectors;

@GrpcService
public class AuthenticateServerService extends AuthenticationServiceImplBase {

  @Autowired private UserRepository repository;

  @Autowired private PasswordService passwordService;

  private final JwtTokenUtil jwtTokenService = JwtTokenUtil.getInstance();

  /** Attempts to authenticate a user with a given username and password. */
  @Override
  public void authenticate(
      AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
    AuthenticateResponse.Builder reply = AuthenticateResponse.newBuilder();

    UserModel user = repository.findByUsername(request.getUsername());
    try {
      if (user != null
          && passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {

        String token =
            jwtTokenService.generateTokenForUser(
                user.getUsername(),
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                // Encodes all roles a user has as a CSV
                user.getRoles().stream()
                    .map(UserRole::toString)
                    .map(String::toLowerCase)
                    .map(s -> s.replace(" ", ""))
                    .collect(Collectors.joining(",")));

        reply
            .setUserId(user.getId())
            .setUsername(user.getUsername())
            .setFirstName(user.getFirstName())
            .setLastName(user.getLastName())
            .setEmail(user.getEmail())
            .setMessage("Logged in successfully!")
            .setSuccess(true)
            .setToken(token);
      } else {
        reply
            .setMessage("Log in attempt failed: username or password incorrect")
            .setSuccess(false)
            .setToken("");
      }
      responseObserver.onNext(reply.build());
      responseObserver.onCompleted();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      e.printStackTrace();
      responseObserver.onError(e);
    }
  }

  /**
   * The AuthenticationInterceptor already handles validating the authState for us, so here we just
   * need to retrieve that from the current context and return it in the gRPC body.
   */
  @Override
  public void checkAuthState(Empty request, StreamObserver<AuthState> responseObserver) {
    responseObserver.onNext(AuthenticationServerInterceptor.AUTH_STATE.get());
    responseObserver.onCompleted();
  }
}
