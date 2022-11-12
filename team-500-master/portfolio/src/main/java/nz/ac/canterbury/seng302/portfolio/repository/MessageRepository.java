package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/** This is the repository for messages. */
public interface MessageRepository extends CrudRepository<MessageEntity, String> {

    Page<MessageEntity> getPaginatedMessagesByConversationId(String conversationId, Pageable request);
}
