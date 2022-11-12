package nz.ac.canterbury.seng302.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.mapping.ConversationMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.ConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@SpringBootTest
class ConversationServiceTest {
  @InjectMocks private ConversationService conversationService;
  @Mock private ConversationRepository conversationRepository;
  @Mock private ConversationMapper conversationMapper;
  @Mock private SimpMessagingTemplate template;

  @BeforeEach
  void beforeEach() {
    var conversation = new ConversationEntity(List.of(1, 2, 3));
    Mockito.when(conversationMapper.toEntity(any())).thenReturn(conversation);
    Mockito.when(conversationMapper.toContract(any()))
        .thenReturn(new ConversationContract(conversation.getId(), null, null, null, null));
  }

  @Test
  void testCreatingConversation() {
    var conversation1 =
        conversationService.createConversation(new BaseConversationContract(List.of(1, 2, 3)));

    assertNotNull(conversation1);
  }

  @Test
  void testUpdateConversation() {
    var conversation1 = new ConversationEntity(List.of(1, 2, 3));
    Mockito.when(conversationRepository.findById(any())).thenReturn(Optional.of(conversation1));
    conversationService.updateConversation(
        new BaseConversationContract(List.of(1, 2)), conversation1.getId());

    assertEquals(List.of(1, 2), conversation1.getUserIds());
  }
}
