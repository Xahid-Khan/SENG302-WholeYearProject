package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.sql.Timestamp;

/**
 * A contract for a message.
 *
 * @param conversationId the conversation this message belongs to
 * @param messageId the message's ID
 * @param sentBy who sent the message (User ID)
 * @param messageContent the content of the message
 * @param timeSent when the message was sent
 */
public record MessageContract(
    String conversationId,
    String messageId,
    int sentBy,
    String senderName,
    String messageContent,
    Timestamp timeSent
) implements Contractable {}
