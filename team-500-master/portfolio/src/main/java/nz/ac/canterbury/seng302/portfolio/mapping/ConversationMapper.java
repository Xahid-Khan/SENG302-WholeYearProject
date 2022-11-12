package nz.ac.canterbury.seng302.portfolio.mapping;

import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.model.contract.ConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.UserContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseConversationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Mapper for conversations from JSON to entity and vice versa. */
@Component
public class ConversationMapper
    implements Mappable<ConversationEntity, BaseConversationContract, ConversationContract> {
  @Autowired private MessageMapper messageMapper;
  @Autowired private UserMapper userMapper;
  @Autowired private UserAccountService userAccountService;

  /**
   * Maps a contract to an entity.
   *
   * @param contract the contract (ideally a base contract) to convert to an entity
   * @return the entity representation of a contract
   */
  @Override
  public ConversationEntity toEntity(BaseConversationContract contract) {
    return new ConversationEntity(contract.userIds());
  }

  /**
   * Maps an entity to a contract.
   *
   * @param entity the entity to map to a contract
   * @return the contract representation of an entity
   */
  @Override
  public ConversationContract toContract(ConversationEntity entity) {
    List<UserContract> userContracts = new ArrayList<>();
    for (Integer userId : entity.getUserIds()) {
      userContracts.add(userMapper.toContract(userAccountService.getUserById(userId)));
    }
    return new ConversationContract(
        entity.getId(),
        userContracts,
        entity.getCreationDate(),
        messageMapper.toContract(entity.getMostRecentMessage()),
        entity.getUserHasReadMessages()
    );
  }
}
