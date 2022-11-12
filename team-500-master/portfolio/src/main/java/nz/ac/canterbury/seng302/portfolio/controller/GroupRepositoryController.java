package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseGroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.GroupRepositoryService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles the GET request on the /groups/repository endpoint.
 */
@Controller
public class GroupRepositoryController extends AuthenticatedController {

  @Autowired
  private GroupRepositoryService groupRepositoryService;
  @Autowired
  private GroupsClientService groupsClientService;

  @Autowired
  public GroupRepositoryController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request, and will produce a list of all the
   * groups.
   *
   * @return List of groups converted into project contract (JSON) type.
   */
  @GetMapping(value = "/groups/all_repository/", produces = "application/json")
  public ResponseEntity<ArrayList<GroupRepositoryContract>> getAll() {
    try {
      var groupRepos = new ArrayList<GroupRepositoryContract>();
      var groupsRepositoryResponse = groupRepositoryService.getAll();
      for (GroupRepositoryContract group : groupsRepositoryResponse) {
        groupRepos.add(group);
      }

      return ResponseEntity.ok(groupRepos);
    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * This method will be invoked when API receives a GET request, get the repo in the params
   *
   * @return List of groups converted into project contract (JSON) type.
   */
  @GetMapping(value = "/groups/repository/{id}/", produces = "application/json")
  public ResponseEntity<GroupRepositoryContract> get(@PathVariable String id) {
    try {
      var result = groupRepositoryService.get(id);

      //if null return 404 else return ok
      return result == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).build()
          : ResponseEntity.ok(result);

    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Creates a new group repository associated with a group
   *
   * @param groupRepositoryContract
   * @return
   */
  @PostMapping
  @RequestMapping(value = "/groups/add_repository", produces = "application/json")
  public ResponseEntity<GroupRepositoryContract> create(
      @RequestBody BaseGroupRepositoryContract groupRepositoryContract) {
    try {
      var result = groupRepositoryService.add(groupRepositoryContract.groupId());

      //if null return 404 else return ok
      return result == null ? ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
          : ResponseEntity.ok(result);

    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Deletes a group repository
   *
   * @param id
   * @return
   */
  @DeleteMapping
  @RequestMapping(value = "/groups/delete_repository/{id}/", produces = "application/json")
  public ResponseEntity<Boolean> delete(@PathVariable Integer id) {
    try {
      var result = groupRepositoryService.delete(id);

      //if null return 404 else return ok
      return result ? ResponseEntity.ok(result)
          : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Updates a group repository
   *
   * @param groupRepositoryContract
   * @return
   */
  @PutMapping
  @RequestMapping(value = "/groups/update_repository", produces = "application/json")
  public ResponseEntity<Boolean> update(
      @RequestBody GroupRepositoryContract groupRepositoryContract) {
    try {
      var result = false;
      if (groupRepositoryContract.longName().length() > 0 && groupRepositoryContract.longName().length() < 64) {
        groupsClientService.updateGroupLongName(groupRepositoryContract.groupId(),
            groupRepositoryContract.longName());
        result = true;
      }
      if (groupRepositoryContract.repositoryId() != null  &&
          groupRepositoryContract.repositoryId() > 0  &&
          groupRepositoryContract.token().length() > 0 &&
          groupRepositoryContract.token().length() < 64 &&
          groupRepositoryContract.alias(). length() > 0) {
        result = groupRepositoryService.update(groupRepositoryContract.groupId(),
            groupRepositoryContract.repositoryId(), groupRepositoryContract.token(),
            groupRepositoryContract.alias());
      }

      //if null return 404 else return ok
      return result ? ResponseEntity.ok().build()
          : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
