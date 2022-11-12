package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.MilestoneContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseMilestoneContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.MilestoneEntity;
import org.springframework.stereotype.Component;

/** Mapper for milestones from JSON to entity and vice versa. */
@Component
public class MilestoneMapper
    implements Mappable<MilestoneEntity, BaseMilestoneContract, MilestoneContract> {
  /**
   * This function converts the received JSON body to the event Entity.
   *
   * @param contract a JSON data received via HTTP body.
   * @return an event entity
   */
  @Override
  public MilestoneEntity toEntity(BaseMilestoneContract contract) {
    return new MilestoneEntity(contract.name(), contract.description(), contract.startDate());
  }

  /**
   * This method receives an event Entity and converts that entity into transferable JSON data type,
   * while doing so it also retrieves all and events related to that event.
   *
   * @param entity a milestone entity that is retried from database
   * @return returns a milestone and related events in JSON data type.
   */
  @Override
  public MilestoneContract toContract(MilestoneEntity entity) {
    return new MilestoneContract(
        entity.getProject().getId(),
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getStartDate(),
        entity.getOrderNumber());
  }
}
