package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.sql.Timestamp;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/** The database representation of a Message. */
@Entity
@Table(name = "message")
public class MessageEntity extends PortfolioEntity {
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ConversationEntity conversation;

  @Column(nullable = false)
  private Integer sentBy;

  @Column(nullable = false)
  private String senderName;

  @Column(nullable = false, length = 4096)
  private String messageContent;

  // Makes the database automatically create the timestamp when the user is inserted
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Timestamp timeSent;

  protected MessageEntity() {}

  public MessageEntity(String messageContent, Integer sentBy, String senderName) {
    this.messageContent = messageContent;
    this.sentBy = sentBy;
    this.senderName = senderName;
  }

  public ConversationEntity getConversation() {
    return conversation;
  }

  public void setConversation(ConversationEntity conversation) {
    this.conversation = conversation;
  }

  public Integer getSentBy() {
    return sentBy;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public void setMessageContent(String messageContent) {
    this.messageContent = messageContent;
  }

  public Timestamp getTimeSent() {
    return timeSent;
  }

  public String getSenderName() {    return senderName; }
}
