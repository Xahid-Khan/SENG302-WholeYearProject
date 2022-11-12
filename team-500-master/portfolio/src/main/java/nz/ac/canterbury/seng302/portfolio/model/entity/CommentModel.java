package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Database schema for a comment on a post
 */
@Entity
public class CommentModel {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private int id;

  @Column(name = "post_id", nullable = false)
  private int postId;

  @Column(name = "user_id", nullable = false)
  private int userId;

  @Column(name = "comment_content", length = 4096)
  private String commentContent;

  // Makes the database automatically create the timestamp when the user is inserted
  @CreationTimestamp
  private Timestamp created;

  public CommentModel(int postId, int userId, String comment) {
    this.postId = postId;
    this.userId = userId;
    this.commentContent = comment;
  }

  protected CommentModel() {
  }

  public int getUserId() {
    return userId;
  }

  public Timestamp getCreated() {
    return created;
  }

  public String getCommentContent() {
    return commentContent;
  }

  public void setCommentContent(String commentContent) {
    this.commentContent = commentContent;
  }

  public int getPostId() {
    return postId;
  }

  public int getId() {
    return id;
  }
}
