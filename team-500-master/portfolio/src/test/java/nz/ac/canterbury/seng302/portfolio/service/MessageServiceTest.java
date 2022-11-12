package nz.ac.canterbury.seng302.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.mapping.MessageMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.MessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMessageContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import nz.ac.canterbury.seng302.portfolio.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest
class MessageServiceTest {
  @InjectMocks private MessageService messageService;
  @Mock private MessageMapper messageMapper;
  @Mock private MessageRepository messageRepository;
  @Mock private ConversationRepository conversationRepository;

  @Mock private SimpMessagingTemplate template;

  @BeforeEach
  void beforeEach() {
    var message = new MessageEntity("Hello", 1, "Jo");

    Mockito.when(messageMapper.toEntity(any())).thenReturn(message);
    Mockito.when(messageMapper.toContract(any()))
        .thenReturn(
            new MessageContract(
                null, message.getId(), message.getSentBy(), message.getSenderName(), message.getMessageContent(), null));
  }

  @Test
  void testCreateMessage() {
    var conversation = new ConversationEntity(List.of(1, 2, 3));
    Mockito.when(conversationRepository.findById(any())).thenReturn(Optional.of(conversation));

    var response = messageService.createMessage("0", new BaseMessageContract("Hello world", 2, "Jo"));

    // Ensure that the message contract is saved
    assertNotNull(response);
    // Ensure that the conversation added the most recent message
    assertNotNull(conversation.getMostRecentMessage());
  }

  @Test
  void testDeleteMessage() {
    var message = new MessageEntity("a", 1, "Jo");
    message.setConversation(new ConversationEntity(List.of(1, 2, 3)));
    Mockito.when(messageRepository.findById(any())).thenReturn(Optional.of(message));
    messageService.deleteMessage(message.getId());
    Mockito.verify(messageRepository, times(1)).delete(any());
  }
}
