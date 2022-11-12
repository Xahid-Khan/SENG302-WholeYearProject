package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.MessageContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMessageContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.MessageEntity;
import org.springframework.stereotype.Component;

/** Mapper for messages from JSON to entity and vice versa. */
@Component
public class MessageMapper
    implements Mappable<MessageEntity, BaseMessageContract, MessageContract> {

  /**
   * Maps a contract to an entity.
   *
   * @param contract the contract (ideally a base contract) to convert to an entity
   * @return the entity representation of a contract
   */
  @Override
  public MessageEntity toEntity(BaseMessageContract contract) {
    return new MessageEntity(contract.messageContent(), contract.sentBy(), contract.senderName());
  }

  /**
   * Maps an entity to a contract.
   *
   * @param entity the entity to map to a contract
   * @return the contract representation of an entity
   */
  @Override
  public MessageContract toContract(MessageEntity entity) {
    if(entity == null){
      return null;
    }
    return new MessageContract(
        entity.getConversation().getId(),
        entity.getId(),
        entity.getSentBy(),
        entity.getSenderName(),
        entity.getMessageContent(),
        entity.getTimeSent());
  }
}
