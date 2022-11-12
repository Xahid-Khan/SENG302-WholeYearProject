package nz.ac.canterbury.seng302.identityprovider.database;


import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

@Entity
public class GroupMemberModel {

  @Id
  private int id = -1;

  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = false)
  private List<Integer> userIds = new ArrayList<>();

  protected GroupMemberModel() {
  }

  public GroupMemberModel(int groupId, List<Integer> userIds) {
    this.id = groupId;
    this.userIds = userIds;
  }

  public int getGroupId() {
    return this.id;
  }

  public List<Integer> getUserIds() {
    return this.userIds;
  }

  public void addNewMember(int userId) {
    this.userIds.add(userId);
  }


  /**
   * Adds users to the group.
   *
   * @param userIds The user ids to add.
   * @return The message if failed. Returns success if successful.
   */
  public String addUserIds(List<Integer> userIds) {
    for (Integer id : userIds) {
      if (!this.userIds.contains(id)) {
        this.userIds.add(id);

      } else {
        return "Error: User already in group";
      }
    }
    return "Success";
  }

  /**
   * Removes users from the group.
   *
   * @param userIds The user ids to add.
   * @return The message if failed. Returns success if successful.
   */
  public String removeUserIds(List<Integer> userIds) {
    for (Integer id : userIds) {
      if (this.userIds.contains(id)) {
        this.userIds.remove(id);

      } else {
        return "Error: User not in group";
      }
    }
    return "Success";

  }
}
