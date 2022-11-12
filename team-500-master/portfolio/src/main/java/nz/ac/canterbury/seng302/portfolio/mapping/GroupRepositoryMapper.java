package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseGroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for milestones from JSON to entity and vice versa.
 */
@Component
public class GroupRepositoryMapper
    implements
    Mappable<GroupRepositoryEntity, BaseGroupRepositoryContract, GroupRepositoryContract> {

  /**
   * This function converts the received JSON body to the event Entity.
   *
   * @param contract a JSON data received via HTTP body.
   * @return an event entity
   */
  @Override
  public GroupRepositoryEntity toEntity(BaseGroupRepositoryContract contract) {
    return new GroupRepositoryEntity(contract.groupId());
  }

  /**
   * This method receives an event Entity and converts that entity into transferable JSON data type,
   * while doing so it also retrieves all and events related to that event.
   *
   * @param entity a milestone entity that is retried from database
   * @return returns a milestone and related events in JSON data type.
   */
  @Override
  public GroupRepositoryContract toContract(GroupRepositoryEntity entity) {
    return new GroupRepositoryContract(entity.getGroupId(), entity.getRepositoryID(),
        entity.getToken(), entity.getAlis(), "");
  }
}
