package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.DeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseDeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** This controller handles all Deadline interactions. */
@RestController
@RequestMapping("/api/v1")
public class DeadlineController extends AuthenticatedController {

  @Autowired private DeadlineService deadlineService;

  @Autowired private ProjectService projectService;

  @Autowired private ValidationService validationService;

  @Autowired private UserAccountService userAccountService;

  @Autowired private AuthStateService authStateService;

  @Autowired private NotificationService notificationService;

  @Autowired private EndDateNotificationService endDateNotificationService;

  @Autowired
  public DeadlineController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request with a deadline ID embedded in URL.
   *
   * @param deadlineId deadline-ID the user wants to retrieve
   * @return a deadline contract (JSON) type of the deadline.
   */
  @GetMapping(value = "/deadlines/{deadlineId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DeadlineContract> getDeadline(@PathVariable String deadlineId) {
    try {
      var deadline = deadlineService.get(deadlineId);

      return ResponseEntity.ok(deadline);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a GET request with a Project ID embedded in URL
   * and will produce all the deadlines of that specific project.
   *
   * @param projectId Project-ID of the project User is interested in
   * @return A list of deadlines of a given project in deadline Contract (JSON) type.
   */
  @GetMapping(
      value = "/projects/{projectId}/deadlines",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<DeadlineContract>> getProjectDeadlines(
      @PathVariable String projectId) {
    try {
      var result = projectService.getById(projectId).deadlines();

      return ResponseEntity.ok(result);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request with a Project ID embedded in URL
   * and data for the deadline in the body.
   *
   * @param projectId Project-ID of the project User wants the deadline to be added to.
   * @return A list of deadlines of a given project in deadline Contract (JSON) type.
   */
  @PostMapping(
      value = "/projects/{projectId}/deadlines",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createDeadline(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String projectId,
      @RequestBody BaseDeadlineContract deadline) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkAddDeadline(projectId, deadline);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Deadline ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        var result = deadlineService.createDeadline(projectId, deadline);
        PaginatedUsersResponse users = userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true);
        UserResponse deadlineCreator = userAccountService.getUserById(authStateService.getId(principal));
        for (UserResponse user: users.getUsersList()) {
          if (user.getId() != deadlineCreator.getId()) {
            notificationService.create(new BaseNotificationContract(user.getId(), "Project", deadlineCreator.getUsername() + " added a new deadline " + deadline.name() + "!"));
          }
        }
        endDateNotificationService.addNotifications(deadline.startDate(), "Deadline", deadline.name(), result.deadlineId());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a PUT request with a Project ID embedded in URL
   * and updated deadline data in the body.
   *
   * @param id Project-ID of the project User wants to make the changes to.
   * @return The updated deadline value
   */
  @PutMapping(value = "/deadlines/{id}")
  public ResponseEntity<?> updateDeadline(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String id,
      @RequestBody BaseDeadlineContract deadline) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkUpdateDeadline(id, deadline);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Deadline ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        deadlineService.update(id, deadline);
        endDateNotificationService.removeNotifications("Deadline" + id);
        endDateNotificationService.addNotifications(deadline.startDate(), "Deadline", deadline.name(), id);
        return ResponseEntity.ok(deadlineService.get(id));
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a DELETE request with a deadline ID embedded in
   * URL.
   *
   * @param id Deadline ID the user wants to delete
   * @return status_Code 204.
   */
  @DeleteMapping(value = "/deadlines/{id}")
  public ResponseEntity<Void> deleteDeadline(
      @AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String id) {
    if (isTeacher(principal)) {
      try {
        deadlineService.delete(id);
        endDateNotificationService.removeNotifications("Deadline" + id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
