package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.EventContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseEventContract;
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

/** This controller handles all Event interactions. */
@RestController
@RequestMapping("/api/v1")
public class EventController extends AuthenticatedController {

  @Autowired private EventService eventService;

  @Autowired private ProjectService projectService;

  @Autowired private ValidationService validationService;

  @Autowired private UserAccountService userAccountService;

  @Autowired private AuthStateService authStateService;

  @Autowired private NotificationService notificationService;

  @Autowired private EndDateNotificationService endDateNotificationService;

  @Autowired
  public EventController(AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request with an event ID embedded in URL.
   *
   * @param eventId event-ID the user wants to retrieve
   * @return an event contract (JSON) type of the event.
   */
  @GetMapping(value = "/events/{eventId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EventContract> getEvent(@PathVariable String eventId) {
    try {
      var event = eventService.get(eventId);

      return ResponseEntity.ok(event);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a GET request with a Project ID embedded in URL
   * and will produce all the events of that specific project.
   *
   * @param projectId Project-ID of the project User is interested in
   * @return A list of events of a given project in Event Contract (JSON) type.
   */
  @GetMapping(value = "/projects/{projectId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<EventContract>> getProjectEvents(@PathVariable String projectId) {
    try {
      var result = projectService.getById(projectId).events();

      return ResponseEntity.ok(result);
    } catch (NoSuchElementException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request with a Project ID embedded in URL
   * and data for the event in the body.
   *
   * @param projectId Project-ID of the project User wants the event to be added to.
   * @return A list of events of a given project in Event Contract (JSON) type.
   */
  @PostMapping(value = "/projects/{projectId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createEvent(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String projectId,
      @RequestBody BaseEventContract event) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkAddEvent(projectId, event);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Event ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        var result = eventService.createEvent(projectId, event);
        PaginatedUsersResponse users = userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true);
        UserResponse eventCreator = userAccountService.getUserById(authStateService.getId(principal));
        for (UserResponse user: users.getUsersList()) {
          if (user.getId() != eventCreator.getId()) {
            notificationService.create(new BaseNotificationContract(user.getId(), "Project", eventCreator.getUsername() + " added a new event " + event.name() +"!"));
          }
        }
        endDateNotificationService.addNotifications(event.endDate(), "Event", event.name(), result.eventId());
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
   * and updated event data in the body.
   *
   * @param id Project-ID of the project User wants to make the changes to.
   * @return The updated event value
   */
  @PutMapping(value = "/events/{id}")
  public ResponseEntity<?> updateEvent(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable String id,
      @RequestBody BaseEventContract event) {
    if (isTeacher(principal)) {
      String errorMessage = validationService.checkUpdateEvent(id, event);
      if (!errorMessage.equals("Okay")) {
        if (errorMessage.equals("Project ID does not exist")
            || errorMessage.equals("Event ID does not exist")) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
      }

      try {
        eventService.update(id, event);
        endDateNotificationService.removeNotifications("Event" + id);
        endDateNotificationService.addNotifications(event.endDate(), "Event", event.name(), id);
        return ResponseEntity.ok(eventService.get(id));
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a DELETE request with an Event ID embedded in
   * URL.
   *
   * @param id Event ID the user wants to delete
   * @return status_Code 204.
   */
  @DeleteMapping(value = "/events/{id}")
  public ResponseEntity<Void> deleteEvent(
      @AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String id) {
    if (isTeacher(principal)) {
      try {
        eventService.delete(id);
        endDateNotificationService.removeNotifications("Event" + id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      } catch (NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
