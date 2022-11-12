package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;


/**
 * Database schema for a post
 */
@Entity
@Table(name = "postmodel")
public class PostModel {

  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "group_id", nullable = false)
  private int groupId;

  @Column(name = "user_id", nullable = false)
  private int userId;

  @Column(name = "post_content", length = 4096)
  private String postContent;

  // Makes the database automatically create the timestamp when the user is inserted
  @CreationTimestamp
  @OrderColumn
  @Column(updatable = false, name = "created")
  private Timestamp created;

  @CreationTimestamp
  private Timestamp updated;

  @ManyToOne
  @JoinColumn(name = "reaction_model_ID")
  private ReactionModel reactionModel;

  protected PostModel() {
  }

  public PostModel(int groupId, int userId, String postContent) {
    this.groupId = groupId;
    this.userId = userId;
    this.postContent = postContent;

    Date date = new Date();
    this.created = new Timestamp(date.getTime());
  }

  public ReactionModel getReactionModel() {
    return reactionModel;
  }

  public void setReactionModel(ReactionModel reactionModel) {
    this.reactionModel = reactionModel;
  }

  public Timestamp getCreated() {
    return this.created;
  }

  public Timestamp getUpdated() {
    return this.updated;
  }

  public String getPostContent() {
    return postContent;
  }

  public void setPostContent(String postContent) {
    this.postContent = postContent;
    Date date = new Date();
    this.updated = new Timestamp(date.getTime());
  }

  public boolean isPostUpdated() {
    return this.updated == null ? false : true;
  }

  public int getUserId() {
    return userId;
  }

  public int getGroupId() {
    return groupId;
  }

  public int getId() {
    return id;
  }
}
