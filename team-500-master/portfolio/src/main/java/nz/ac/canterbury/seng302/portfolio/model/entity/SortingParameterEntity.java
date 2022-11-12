package nz.ac.canterbury.seng302.portfolio.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** This entity class handles everything to do with a user owning sorting parameters. */
@Entity
@Table(name = "parameters")
public class SortingParameterEntity {
  @Id private int userId;

  @Column private String sortAttribute;

  @Column private boolean sortOrder;

  protected SortingParameterEntity() {}

  /**
   * Creates a new SortingParameterEntity bound to a userId.
   *
   * @param userId the userID of whom owns these sorting parameters
   * @param sortAttribute the attribute to be sorted for
   * @param sortOrder the sorting direction
   */
  public SortingParameterEntity(int userId, String sortAttribute, boolean sortOrder) {
    this.userId = userId;
    this.sortAttribute = sortAttribute;
    this.sortOrder = sortOrder;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int id) {
    this.userId = id;
  }

  public String getSortAttribute() {
    return sortAttribute;
  }

  public boolean isSortOrder() {
    return sortOrder;
  }
}
