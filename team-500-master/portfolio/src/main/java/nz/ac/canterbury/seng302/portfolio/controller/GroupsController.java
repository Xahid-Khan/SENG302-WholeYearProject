package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.UserContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseGroupContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.GroupRepositoryService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.SubscriptionService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.CreateGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.DeleteGroupResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedGroupsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

/**
 * Handles the GET request on the /groups endpoint.
 */
@Controller
@RequestMapping("/api/v1")
public class GroupsController extends AuthenticatedController {

  @Autowired
  private GroupsClientService groupsClientService;
  @Autowired
  private SubscriptionService subscriptionService;
  @Autowired
  private GroupRepositoryService groupsRepositoryService;
  @Autowired
  private GroupRepositoryService groupRepositoryService;

  @Autowired
  public GroupsController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method will be invoked when API receives a GET request, and will produce a list of all the
   * groups.
   *
   * @return List of groups converted into project contract (JSON) type.
   */
  @GetMapping(value = "/groups/all", produces = "application/json")
  public ResponseEntity<?> getAll(@AuthenticationPrincipal PortfolioPrincipal principal) {
    try {
      PaginatedGroupsResponse groupsResponse = groupsClientService.getAllGroupDetails();
      List<GroupDetailsResponse> groupsList = groupsResponse.getGroupsList();
      ArrayList<GroupContract> groups = new ArrayList<>();
      for (GroupDetailsResponse groupDetails : groupsList) {
        boolean userCanEdit =
            (groupsClientService.isMemberOfTheGroup(getUserId(principal), groupDetails.getGroupId())
                || isTeacher(principal));
        GroupRepositoryContract groupRepoData = groupRepositoryService.getRepoByGroupId(
            groupDetails.getGroupId());

        List<ResponseEntity> repoData = getRepoData(groupRepoData.repositoryId(),
            groupRepoData.token());
        groups.add(
            new GroupContract(
                groupDetails.getGroupId(),
                groupDetails.getShortName(),
                groupDetails.getLongName(),
                groupRepoData.alias(),
                groupRepoData.repositoryId(),
                groupRepoData.token() == "" ? "" :
                        repoData.get(0).getStatusCode().is2xxSuccessful() ? groupRepoData.token() : "INVALID",
                userCanEdit,
                getUsers(groupDetails.getMembersList()),
                repoData.get(0).getStatusCode().is2xxSuccessful() ? repoData.get(0).getBody()
                    : new ArrayList<>(),
                repoData.get(1).getStatusCode().is2xxSuccessful() ? repoData.get(1).getBody()
                    : new ArrayList<>()
            ));
      }
      return ResponseEntity.ok(groups);
    } catch (NoSuchElementException error) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  /**
   * Helper method to convert a list of UserResponse objects into a list of UserContract objects.
   *
   * @param userList
   * @return
   */
  private ArrayList<UserContract> getUsers(List<UserResponse> userList) {
    ArrayList<UserContract> users = new ArrayList<>();
    for (UserResponse user : userList) {
      users.add(
          new UserContract(
              user.getId(),
              user.getFirstName(),
              user.getMiddleName(),
              user.getLastName(),
              user.getNickname(),
              user.getUsername(),
              user.getEmail(),
              user.getPersonalPronouns(),
              user.getBio(),
              user.getRolesList()));
    }
    return users;
  }

  /**
   * This method will be invoked when API receives a POST request to delete members. Invokes the
   * client service to delete the members
   *
   * @param groupId The group id to delete members from
   * @param members The members to delete from the group
   */
  @PostMapping(value = "/groups/{groupId}/delete-members", produces = "application/json")
  public ResponseEntity<?> deleteMembers(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable("groupId") String groupId,
      @RequestBody List<Integer> members) {
    if (isTeacher(principal)) {
      try {
        groupsClientService.removeGroupMembers(Integer.parseInt(groupId), members);
        return ResponseEntity.ok().build();
      } catch (Exception error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request to add members. Invokes the client
   * service to add the members
   *
   * @param groupId The group id to delete members from
   * @param members The members to delete from the group
   */
  @PostMapping(value = "/groups/{groupId}/add-members", produces = "application/json")
  public ResponseEntity<?> addMembers(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable("groupId") String groupId,
      @RequestBody List<Integer> members) {

    if (isTeacher(principal)) {
      try {
        groupsClientService.addGroupMembers(Integer.parseInt(groupId), members);
        members.stream().forEach(member -> subscriptionService.subscribe(
            new SubscriptionContract(member, Integer.parseInt(groupId))));
        return ResponseEntity.ok().build();
      } catch (Exception error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request to delete the group. Invokes the
   * client service to delete the group
   *
   * @param groupId The group id to delete
   */
  @DeleteMapping(value = "/groups/{groupId}", produces = "application/json")
  public ResponseEntity<?> deleteGroup(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable("groupId") String groupId) {
    if (isTeacher(principal)) {
      try {
        DeleteGroupResponse response = groupsClientService.deleteGroup(Integer.parseInt(groupId));
        if (response.getIsSuccess()) {
          //associates a group repository with the group
          groupsRepositoryService.delete(Integer.parseInt(groupId));
          return ResponseEntity.ok().build();
        } else {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
      } catch (Exception error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  /**
   * This method will be invoked when API receives a POST request to create a group. Invokes the
   * client service to create the group
   *
   * @param newGroup The contract for the group to create
   */
  @PostMapping(value = "/groups", produces = "application/json")
  public ResponseEntity<?> createGroup(
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody BaseGroupContract newGroup) {
    if (isTeacher(principal)) {
      try {
        CreateGroupResponse response = groupsClientService.createGroup(newGroup);
        if (response.getIsSuccess()) {
          //associates a group repository with the group
          groupsRepositoryService.add(response.getNewGroupId());
          return ResponseEntity.ok().build();
        } else {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Oh dear");
        }
      } catch (Exception error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }


  public ArrayList<ResponseEntity> getRepoData(Integer repositoryId, String token) {
    var result = new ArrayList<ResponseEntity>();
    String gitLabBranches =
        "https://eng-git.canterbury.ac.nz/api/v4/projects/" + repositoryId
            + "/repository/branches";
    String gitLabCommits =
        "https://eng-git.canterbury.ac.nz/api/v4/projects/" + repositoryId
            + "/repository/commits";

    HttpHeaders headers = new HttpHeaders();
    headers.add("PRIVATE-TOKEN", token);
    HttpEntity<Object> entity = new HttpEntity<>(headers);
    RestTemplate getRepoData = new RestTemplate();

    ResponseEntity<Object[]> branches;
    ResponseEntity<Object[]> commits;
    try {
      branches = getRepoData.exchange(gitLabBranches, HttpMethod.GET, entity, Object[].class);
      commits = getRepoData.exchange(gitLabCommits, HttpMethod.GET, entity, Object[].class);
    } catch (Exception e) {
      branches = ResponseEntity.badRequest().build();
      commits = ResponseEntity.badRequest().build();
    }

    result.add(branches);
    result.add(commits);
    return result;
  }

}
