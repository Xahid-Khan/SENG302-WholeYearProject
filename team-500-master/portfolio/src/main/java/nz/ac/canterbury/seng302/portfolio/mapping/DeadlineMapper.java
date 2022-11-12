package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseDeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.DeadlineContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.DeadlineEntity;
import org.springframework.stereotype.Component;

/** Mapper for deadlines from JSON to entity and vice versa. */
@Component
public class DeadlineMapper
    implements Mappable<DeadlineEntity, BaseDeadlineContract, DeadlineContract> {
  /**
   * This function converts the received JSON body to the deadline Entity.
   *
   * @param contract a JSON data received via HTTP body.
   * @return an deadline entity
   */
  @Override
  public DeadlineEntity toEntity(BaseDeadlineContract contract) {
    return new DeadlineEntity(contract.name(), contract.description(), contract.startDate());
  }

  /**
   * This method receives an deadline Entity and converts that entity into transferable JSON data
   * type, while doing so it also retrieves all and deadlines related to that deadline.
   *
   * @param entity a deadline entity that is retried from database
   * @return returns a deadline and related deadlines in JSON data type.
   */
  @Override
  public DeadlineContract toContract(DeadlineEntity entity) {
    return new DeadlineContract(
        entity.getProject().getId(),
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getStartDate(),
        entity.getOrderNumber()
    );
  }
}
