package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.MilestoneContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMilestoneContract;
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

/** This controller handles all Milestone interactions. */
@RestController
@RequestMapping("/api/v1")
public class MilestoneController extends AuthenticatedController {

  @Autowired private MilestoneService milestoneService;

  @Autowired private ProjectService projectService;

  @Autowired private ValidationService validationService;

  @Autowired private UserAccountService userAccountService;

  @Autowired private AuthStateService authStateService;

  @Autowired private NotificationService notificationService;

  @Autowired private EndDateNotificationService endDateNotificationService;

  @Autowired
  public MilestoneController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request with a milestone ID embedded in
   * URL.
   *
   * @param milestoneId milestone-ID the user wants to retrieve
   * @return a milestone contract (JSON) type of the milestone.
   */
  @GetMapping(value = "/milestones/{milestoneId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MilestoneContract> getMilestone(@PathVariable String milestoneId) {
    try {
      var milestone = milestoneService.get(milestoneId);

      return ResponseEntity.ok(milestone);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a GET request with a Project ID embedded in URL
   * and will produce all the milestones of that specific project.
   *
   * @param projectId Project-ID of the project User is interested in
   * @return A list of milestones of a given project in milestone Contract (JSON) type.
   */
  @GetMapping(
      value = "/projects/{projectId}/milestones",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<MilestoneContract>> getProjectMilestones(
      @PathVariable String projectId) {
    try {
      var result = projectService.getById(projectId).milestones();

      return ResponseEntity.ok(result);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request with a Project ID embedded in URL
   * and data for the milestone in the body.
   *
   * @param projectId Project-ID of the project User wants the milestone to be added to.
   * @return A list of milestones of a given project in milestone Contract (JSON) type.
   */
  @PostMapping(
      value = "/projects/{projectId}/milestones",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createMilestone(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String projectId,
      @RequestBody BaseMilestoneContract milestone) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkAddMilestone(projectId, milestone);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Milestone ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        var result = milestoneService.createMilestone(projectId, milestone);
        PaginatedUsersResponse users = userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true);
        UserResponse milestoneCreator = userAccountService.getUserById(authStateService.getId(principal));
        for (UserResponse user: users.getUsersList()) {
          if (user.getId() != milestoneCreator.getId()) {
            notificationService.create(new BaseNotificationContract(user.getId(), "Project", milestoneCreator.getUsername() + " added a new milestone " + milestone.name() + "!"));
          }
        }
        endDateNotificationService.addNotifications(milestone.startDate(), "Milestone", milestone.name(), result.milestoneId());
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
   * and updated milestone data in the body.
   *
   * @param id Project-ID of the project User wants to make the changes to.
   * @return The updated milestone value
   */
  @PutMapping(value = "/milestones/{id}")
  public ResponseEntity<?> updateMilestone(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String id,
      @RequestBody BaseMilestoneContract milestone) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkUpdateMilestone(id, milestone);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Milestone ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        milestoneService.update(id, milestone);
        endDateNotificationService.removeNotifications("Milestone" + id);
        endDateNotificationService.addNotifications(milestone.startDate(), "Milestone", milestone.name(), id);
        return ResponseEntity.ok(milestoneService.get(id));
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a DELETE request with a Milestone ID embedded in
   * URL.
   *
   * @param id Milestone ID the user wants to delete
   * @return status_Code 204.
   */
  @DeleteMapping(value = "/milestones/{id}")
  public ResponseEntity<Void> deleteMilestone(
      @AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String id) {
    if (isTeacher(principal)) {
      try {
        milestoneService.delete(id);
        endDateNotificationService.removeNotifications("Milestone" + id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
