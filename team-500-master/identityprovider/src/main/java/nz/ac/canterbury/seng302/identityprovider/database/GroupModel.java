package nz.ac.canterbury.seng302.identityprovider.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Handles having groups as models for the database.
 */
@Entity
public class GroupModel {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(unique = true, nullable = false)
  private String shortName;

  @Column(unique = true, nullable = false)
  private String longName;

  protected GroupModel() {}

  public GroupModel(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }

  public int getId() {
    return id;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public void setLongName(String longName) {
    this.longName = longName;
  }
}
