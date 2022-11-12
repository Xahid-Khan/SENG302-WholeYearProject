package nz.ac.canterbury.seng302.portfolio.model.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/** The database representation of a Conversation. */
@Entity
@Table(name = "conversation")
public class ConversationEntity extends PortfolioEntity {
  @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
  @Fetch(value = FetchMode.SUBSELECT)
  // This ensures that some collection operations do not trigger collection initialization
  //  Read more:
  // https://sites.google.com/a/pintailconsultingllc.com/java/hibernate-extra-lazy-collection-fetching
  @LazyCollection(LazyCollectionOption.EXTRA)
  @OrderBy(value = "timeSent desc")
  private final List<MessageEntity> messages = new ArrayList<>();
  // Eager since loading all IDs always is vital
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = false)
  // TODO: Extend this to add "seen" field OR new ConversationSeen entity tying userId,
  // conversationId,
  //  and seenCount together
  private List<Integer> userIds = new ArrayList<>();
  // Makes the database automatically create the timestamp when the user is inserted
  @CreationTimestamp
  @Column(name = "creationDate", nullable = false, updatable = false)
  private Timestamp creationDate;

  private Timestamp mostRecentMessageTimestamp;

  @ElementCollection(fetch = FetchType.LAZY)
  @Column(nullable = false)
  private List<Integer> userHasReadMessages = new ArrayList<>();

  protected ConversationEntity() {}

  /**
   * Creates a conversation out of an existing amount of user IDs.
   *
   * @param userIds the user IDs to add to the conversation initially
   */
  public ConversationEntity(List<Integer> userIds) {

    this.userIds = userIds;

  }

  public List<Integer> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<Integer> userIds) {
    this.userIds = userIds;
  }

  /**
   * Returns the most recent message, used for previewing.
   *
   * @return the most recent message for previewing, or null if there are no messages
   */
  public MessageEntity getMostRecentMessage() {
    return !messages.isEmpty() ? messages.get(0) : null;
  }

  /**
   * Adds a message into the conversation. This should be done before a message is saved (to get the
   * conversation for the message) After, the message needs to be saved into the MessageRepository.
   * Once both are done, setMostRecentMessageTimestamp() needs to be called, and finally a save to
   * the ConversationRepository with this conversation.
   *
   * @param message the message to add to the conversation
   */
  public void addMessage(MessageEntity message) {
    messages.add(message);
    message.setConversation(this);
  }

  /**
   * Sets the most recent timestamp based on the last message sent. This *must* be done after the
   * message is saved into the message repository, since otherwise the timestamp of the message is
   * not generated.
   */
  public void setMostRecentMessageTimestamp() {
    MessageEntity mostRecentMessage = getMostRecentMessage();
    mostRecentMessageTimestamp = mostRecentMessage != null ? mostRecentMessage.getTimeSent() : null;
  }

  /**
   * Removes a message from the conversation.
   *
   * @param message the message to remove from the conversation
   */
  public void removeMessage(MessageEntity message) {
    messages.remove(message);
    message.setConversation(null);
    MessageEntity mostRecentMessage = getMostRecentMessage();
    mostRecentMessageTimestamp = mostRecentMessage != null ? mostRecentMessage.getTimeSent() : null;
  }

  public Timestamp getCreationDate() {
    return creationDate;
  }

  public Timestamp getMostRecentMessageTimestamp() {
    return mostRecentMessageTimestamp;
  }

  public List<Integer> getUserHasReadMessages() {
    return userHasReadMessages;
  }

  public void setUserHasReadMessages(List<Integer> userHasReadMessages) {
    this.userHasReadMessages = userHasReadMessages;
  }
}
