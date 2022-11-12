package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.model.contract.NotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.NotificationService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles the notifications api calls.
 */
@RestController
@RequestMapping("/api/v1")
public class NotificationsController extends AuthenticatedController {

  @Autowired
  private NotificationService service;

  @Autowired
  protected NotificationsController(AuthStateService authStateService,
      UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request, and will produce a list of all the
   * notifications.
   *
   * @return List of notifications converted into notification contract (JSON) type.
   */
  @GetMapping(value = "/notifications/{id}", produces = "application/json")
  public ResponseEntity<?> getAllNotifications(@PathVariable String id) {
    try {
      List<NotificationContract> contracts = service.getAll(Integer.parseInt(id));
      return ResponseEntity.ok(contracts);
    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (NumberFormatException error) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    } catch (Exception error) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request with data of new notification
   * embedded in body - (JSON type).
   *
   * @param baseContract data of new notification
   * @return a notification contract (JSON) type of the newly created notification.
   */
  @PostMapping(value = "/notifications", produces = "application/json")
  public ResponseEntity<?> addNotification(
      @RequestBody BaseNotificationContract baseContract) {
    try {
      var contract = service.create(baseContract);
      return ResponseEntity.ok(contract);
    } catch (Exception error) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request with the users' id as a path
   * variable
   *
   * @return a notification contract (JSON) type of the newly created notification.
   */
  @PostMapping(value = "/notifications/seen/{userId}", produces = "application/json")
  public ResponseEntity<?> markAllNotificationsAsSeen(
      @PathVariable int userId) {
    try {
      service.setNotificationsSeen(userId);
      return ResponseEntity.ok().build();
    } catch (Exception error) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }
}
