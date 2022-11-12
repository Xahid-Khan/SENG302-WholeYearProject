package nz.ac.canterbury.seng302.portfolio.model.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import nz.ac.canterbury.seng302.portfolio.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
class ConversationRepositoryTest {

  private final ConversationEntity conversation1 = new ConversationEntity(List.of(1, 2, 3));
  private final ConversationEntity conversation2 = new ConversationEntity(List.of(1, 2));
  private final ConversationEntity conversation3 = new ConversationEntity(List.of(2, 3));
  private final Pageable request =
      PageRequest.of(0, 3, Sort.by("mostRecentMessageTimestamp").descending());
  @Autowired private ConversationRepository conversationRepository;
  @Autowired private MessageRepository messageRepository;

  @BeforeEach
  void beforeEach() {
    conversationRepository.deleteAll();
    messageRepository.deleteAll();
    conversationRepository.saveAll(List.of(conversation1, conversation2, conversation3));
  }

  @Test
  void testGetPaginatedPostsByUserIdsInAllConversations() {
    var result =
        conversationRepository.getPaginatedPostsByUserIdsIn(List.of(2), request).getTotalElements();
    // Ensure all 3 conversations are returned
    assertEquals(3, result);
  }

  @Test
  void testGetPaginatedPostsByUserIdsInTwoConversations() {
    var result =
        conversationRepository.getPaginatedPostsByUserIdsIn(List.of(3), request).getTotalElements();
    // Ensure all 2 conversations are returned
    assertEquals(2, result);
  }

  @Test
  void testEnsureMostRecentConversationIsDisplayedFirstWhenAllSame() {
    var result = conversationRepository.getPaginatedPostsByUserIdsIn(List.of(3), request);
    assertEquals(conversation1.getId(), result.getContent().get(0).getId());
  }

  @Test
  void testEnsureMostRecentConversationIsDisplayedFirstWhenUpdated() {
    var message = new MessageEntity("Hello world", 2, "Jo");
    conversation3.addMessage(message);
    messageRepository.save(message);
    conversation3.setMostRecentMessageTimestamp();
    conversationRepository.save(conversation3);
    var result = conversationRepository.getPaginatedPostsByUserIdsIn(List.of(3), request);

    assertEquals(conversation3.getId(), result.getContent().get(0).getId());
  }
}
