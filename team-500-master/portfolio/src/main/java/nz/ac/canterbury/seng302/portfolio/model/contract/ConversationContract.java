package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.sql.Timestamp;
import java.util.List;

/**
 * A contract for a conversation. The most recent message only is included for previewing.
 *
 * @param conversationId the conversation's ID
 * @param users the users in the conversation, all represented with a UserContract
 * @param creationDate the creation date of the conversation
 * @param mostRecentMessage the most recent message in the conversation for previewing
 */
public record ConversationContract(
    String conversationId,
    List<UserContract> users,
    Timestamp creationDate,
    MessageContract mostRecentMessage,
    List<Integer> userHasReadMessages
) implements Contractable {}
