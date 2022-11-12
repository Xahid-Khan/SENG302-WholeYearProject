package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.mapping.GroupRepositoryMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * A service that manages CRUD operations for group repository settings.
 */
@Service
public class GroupRepositoryService {

  @Autowired
  private GroupRepositoryRepository groupRepositoryRepository;

  @Autowired
  private GroupRepositoryMapper groupRepositoryMapper;

  @Autowired
  private SimpMessagingTemplate template;

  @Autowired
  private PostService postService;


  /**
   * Retrieve the group repository with the given ID.
   *
   * @param id of the contract to get
   * @return GroupRepository with the given ID
   * @throws NoSuchElementException if the id is invalid
   */
  public GroupRepositoryContract get(String id) {
    if (groupRepositoryRepository.existsById(id)) {
      return null;
    }
    var groupRepository = groupRepositoryRepository.findById(id).orElseThrow();
    return groupRepositoryMapper.toContract(groupRepository);
  }

  /**
   * This method returns a group repository entity as a group repository contract for a given group
   * ID.
   *
   * @param groupId group ID of type integer.
   * @return GroupRepositoryContract
   */
  public GroupRepositoryContract getRepoByGroupId(int groupId) {
    var data = groupRepositoryRepository.getAllByGroupId(groupId);
    if (data != null) {
      return groupRepositoryMapper.toContract(data);
    } else {
      return new GroupRepositoryContract(-1, -1, "", "", "");
    }
  }


  /**
   * Retrieve all group repositories.
   *
   * @return List of all group repositories
   */
  public List<GroupRepositoryContract> getAll() {
    Iterable<GroupRepositoryEntity> result = groupRepositoryRepository.findAll();

    ArrayList<GroupRepositoryContract> allRepos = new ArrayList<>();

    for (GroupRepositoryEntity repo : result) {
      allRepos.add(groupRepositoryMapper.toContract(repo));
    }

    return allRepos;
  }

  /**
   * Adds a group repository to the database with the given ID.
   *
   * @param id to associate with the group repository (this should be done on group creation)
   */
  public GroupRepositoryContract add(int id) {
    //checks if the group repository already exists
    if (groupRepositoryRepository.existsByGroupId(id)) {
      return null;
    }
    var groupRepository = new GroupRepositoryEntity(id);
    groupRepositoryRepository.save(groupRepository);
    template.convertAndSend("/topic/groups", "update");

    return groupRepositoryMapper.toContract(groupRepository);
  }

  /**
   * Deletes a group repository from the database with the given ID.
   */
  public boolean delete(int id) {
    //checks if the group repository exists
    if (!groupRepositoryRepository.existsById(Integer.toString(id))) {
      return false;
    }
    groupRepositoryRepository.deleteById(Integer.toString(id));
    postService.deleteAllPostWithGroupId(id);
    template.convertAndSend("/topic/groups", "update");
    return true;
  }

  /**
   * Updates a group repository in the database with the given ID. Sets the repositoryID and token.
   *
   * @param groupId      A group ID of type Integer.
   * @param repositoryID A repository ID of type Integer.
   * @param token        A valid token of type String.
   * @param alias        AN alias for repository of type String.
   * @return returns True if saves successfully False otherwise.
   */
  public boolean update(int groupId, int repositoryID, String token, String alias) {
    //checks if the group repository exists
    try {
      if (!groupRepositoryRepository.existsByGroupId(groupId)) {
        var newGroupRepository = new GroupRepositoryEntity(groupId, repositoryID, token, alias);
        groupRepositoryRepository.save(newGroupRepository);
        return true;
      }
      var groupRepository = groupRepositoryRepository.getAllByGroupId(groupId);
      groupRepository.setAlias(alias);
      groupRepository.setRepositoryID(repositoryID);
      groupRepository.setToken(token);
      groupRepositoryRepository.save(groupRepository);
      template.convertAndSend("/topic/groups", "update");
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }


}
