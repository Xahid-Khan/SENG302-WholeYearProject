package nz.ac.canterbury.seng302.portfolio.service;

import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.mapping.ConversationMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.ConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/** This service handles interacting with the repository for conversations. */
@Service
public class ConversationService {
  @Autowired private ConversationRepository conversationRepository;

  @Autowired private ConversationMapper conversationMapper;

  @Autowired private SimpMessagingTemplate template;

  /**
   * Creates a new conversation based on a base contract.
   *
   * @param baseConversationContract the base contract to create the conversation from
   * @return a full ConversationContract
   */
  public ConversationContract createConversation(
      BaseConversationContract baseConversationContract) {
    ConversationEntity conversation = conversationMapper.toEntity(baseConversationContract);
    template.convertAndSend("/topic/notification", conversation.getUserIds());
    conversation.getUserHasReadMessages().addAll(conversation.getUserIds());
    conversationRepository.save(conversation);
    return conversationMapper.toContract(conversation);
  }

  /**
   * Gets paginated conversations using a PageRequest.
   *
   * @param userId the user ID to get paginated conversations for
   * @param page which page of the data to load (I.E., 0 will load 0 - limit)
   * @param limit the limit of posts to grab, must be greater than 0
   * @return paginated conversations
   */
  public Page<ConversationEntity> getPaginatedConversations(int userId, int page, int limit) {
    Pageable request = PageRequest.of(page, Integer.MAX_VALUE, Sort.by("mostRecentMessageTimestamp").descending());
    return conversationRepository.getPaginatedPostsByUserIdsIn(List.of(userId), request);
  }

  /**
   * Updates a conversation, which only updates its members.
   *
   * @param baseConversationContract the base contract for a conversation
   * @param conversationId the conversation's ID
   * @return the updated ConversationContract
   * @throws NoSuchElementException if the id is invalid
   */
  public ConversationContract updateConversation(
      BaseConversationContract baseConversationContract, String conversationId) {
    ConversationEntity conversation = conversationRepository.findById(conversationId).orElseThrow();
    conversation.setUserIds(baseConversationContract.userIds());
    conversationRepository.save(conversation);
    return conversationMapper.toContract(conversation);
  }

  public boolean isInConversation(Integer userId, String conversationId) {
    ConversationEntity conversation = conversationRepository.findById(conversationId).orElseThrow();
    return conversation.getUserIds().contains(userId);
  }

  public void userReadMessages(int userId, String conversationId) {
    ConversationEntity conversation = conversationRepository.findById(conversationId).orElseThrow();
    if(conversation.getUserHasReadMessages().contains(userId)){
      return;
    }
    conversation.getUserHasReadMessages().add(userId);
    conversationRepository.save(conversation);

  }
}
