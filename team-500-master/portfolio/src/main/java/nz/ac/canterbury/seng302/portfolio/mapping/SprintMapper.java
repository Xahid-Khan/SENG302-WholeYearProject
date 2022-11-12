package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.SprintContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseSprintContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.SprintEntity;
import org.springframework.stereotype.Component;

/** Mapper for sprints from JSON to entity and vice versa. */
@Component
public class SprintMapper implements Mappable<SprintEntity, BaseSprintContract, SprintContract> {
  /**
   * This function converts the received JSON body to the sprint Entity.
   *
   * @param contract a JSON data received via HTTP body.
   * @return a sprint entity
   */
  @Override
  public SprintEntity toEntity(BaseSprintContract contract) {
    return new SprintEntity(
        contract.name(),
        contract.description(),
        contract.startDate(),
        contract.endDate(),
        contract.colour());
  }

  /**
   * This method receives a sprint Entity and converts that entity into transferable JSON data type,
   * while doing so it also retrieves all and events related to that sprint.
   *
   * @param entity a sprint entity that is retried from database
   * @return returns a sprint and related sprints in JSON data type.
   */
  @Override
  public SprintContract toContract(SprintEntity entity) {
    return new SprintContract(
        entity.getProject().getId(),
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getColour(),
        entity.getOrderNumber());
  }
}
