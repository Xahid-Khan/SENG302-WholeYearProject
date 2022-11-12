package nz.ac.canterbury.seng302.portfolio.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import nz.ac.canterbury.seng302.portfolio.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConversationEntityTest {
  @Autowired private ConversationRepository conversationRepository;

  @Autowired private MessageRepository messageRepository;

  @BeforeEach
  public void beforeEach() {
    messageRepository.deleteAll();
    conversationRepository.deleteAll();
  }

  @Test
  void testGettingMostRecentMessage() {
    ConversationEntity conversation = new ConversationEntity(List.of(1));
    conversationRepository.save(conversation);

    // Ensures that if there are no messages, null is returned
    assertNull(conversation.getMostRecentMessage());

    MessageEntity message = new MessageEntity("Testing!", 1, "John");
    conversation.addMessage(message);
    messageRepository.save(message);

    // Ensure that the getting the most recent message works
    assertEquals(
        message.getMessageContent(), conversation.getMostRecentMessage().getMessageContent());
  }
}
