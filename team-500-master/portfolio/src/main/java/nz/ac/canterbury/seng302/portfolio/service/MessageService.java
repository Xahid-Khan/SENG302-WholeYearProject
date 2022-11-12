package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.mapping.MessageMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.MessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMessageContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import nz.ac.canterbury.seng302.portfolio.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  @Autowired private ConversationRepository conversationRepository;
  @Autowired private MessageRepository messageRepository;
  @Autowired private MessageMapper messageMapper;
  @Autowired private SimpMessagingTemplate template;

  /**
   * Creates a message, returning a message contract.
   *
   * @param conversationId the conversation to add the message to
   * @param baseMessageContract the message's contract
   * @return the message that was created
   * @throws NoSuchElementException if the conversation ID is incorrect, or it cannot be found
   */
  public MessageContract createMessage(
      String conversationId, BaseMessageContract baseMessageContract) {
    ConversationEntity conversation = conversationRepository.findById(conversationId).orElseThrow();
    MessageEntity message = messageMapper.toEntity(baseMessageContract);
    conversation.addMessage(message);
    messageRepository.save(message);
    conversation.setMostRecentMessageTimestamp();
    conversation.setUserHasReadMessages(new ArrayList<>(message.getSentBy()));
    conversationRepository.save(conversation);
    template.convertAndSend("/topic/notification", conversation.getUserIds());

    return messageMapper.toContract(message);
  }

  /**
   * Deletes a message, removing it from the conversation.
   *
   * @param messageId the message ID to delete
   * @throws NoSuchElementException if the ID is invalid
   */
  public void deleteMessage(String messageId) {
    MessageEntity message = messageRepository.findById(messageId).orElseThrow();
    ConversationEntity conversation = message.getConversation();
    template.convertAndSend("/topic/notification", conversation.getUserIds());
    conversation.removeMessage(message);
    messageRepository.delete(message);
    conversationRepository.save(conversation);
  }

  public Page<MessageEntity> getPaginatedMessages(String conversationId, int page, int limit) {
    Pageable request = PageRequest.of(page, Integer.MAX_VALUE, Sort.by("timeSent").ascending());
    return messageRepository.getPaginatedMessagesByConversationId(conversationId, request);
  }
}
