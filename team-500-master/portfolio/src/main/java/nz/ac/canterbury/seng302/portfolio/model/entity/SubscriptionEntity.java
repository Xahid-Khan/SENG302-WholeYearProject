package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents a subscription of a user to a group in the database
 */
@Entity
@Table(name = "subscription")
public class SubscriptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "user_id", nullable = false)
  private int userId;


  @Column(name = "group_id", nullable = false)
  private int groupId;

  @CreationTimestamp
  @Column(name = "time_subscribed", updatable = false)
  private Timestamp timeSubscribed;

  protected SubscriptionEntity() {

  }

  public SubscriptionEntity(int userId, int groupId) {
    this.userId = userId;
    this.groupId = groupId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getGroupId() {
    return groupId;
  }

  public void setGroupId(int groupId) {
    this.groupId = groupId;
  }

  public Timestamp getTimeSubscribed() {
    return timeSubscribed;
  }

  public void setTimeSubscribed(Timestamp timeSubscribed) {
    this.timeSubscribed = timeSubscribed;
  }

  public int getId() {
    return this.id;
  }
}