package nz.ac.canterbury.seng302.portfolio.mapping;

import java.util.ArrayList;

import nz.ac.canterbury.seng302.portfolio.model.contract.*;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseProjectContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Mapper for projects from JSON to entity and vice versa. */
@Component
public class ProjectMapper
    implements Mappable<ProjectEntity, BaseProjectContract, ProjectContract> {

  @Autowired private SprintMapper sprintMapper;

  @Autowired private EventMapper eventMapper;

  @Autowired private MilestoneMapper milestoneMapper;

  @Autowired private DeadlineMapper deadlineMapper;

  /**
   * This function converts the received JSON body to the Project Entity.
   *
   * @param contract a JSON data received via HTTP body.
   * @return a project entity
   */
  @Override
  public ProjectEntity toEntity(BaseProjectContract contract) {
    return new ProjectEntity(
        contract.name(), contract.description(), contract.startDate(), contract.endDate());
  }

  /**
   * This method receives a project Entity and converts that entity into transferable JSON data
   * type, while doing so it also retrieves all the sprints and events related to that project.
   *
   * @param entity a project entity that is retried from database
   * @return returns a project and related sprints in JSON data type.
   */
  @Override
  public ProjectContract toContract(ProjectEntity entity) {
    var sprintEntities = entity.getSprints();
    var sprintContracts = new ArrayList<SprintContract>();

    for (int i = 0; i < sprintEntities.size(); i++) {
      sprintContracts.add(sprintMapper.toContract(sprintEntities.get(i)));
    }

    var eventEntities = entity.getEvents();
    var eventContracts = new ArrayList<EventContract>();

    for (int i = 0; i < eventEntities.size(); i++) {
      eventContracts.add(eventMapper.toContract(eventEntities.get(i)));
    }

    var deadlineEntities = entity.getDeadlines();
    var deadlineContracts = new ArrayList<DeadlineContract>();

    for (int i = 0; i < deadlineEntities.size(); i++) {
      deadlineContracts.add(deadlineMapper.toContract(deadlineEntities.get(i)));
    }

    var milestoneEntities = entity.getMilestones();
    var milestoneContracts = new ArrayList<MilestoneContract>();

    for (int i = 0; i < milestoneEntities.size(); i++) {
      milestoneContracts.add(milestoneMapper.toContract(milestoneEntities.get(i)));
    }

    return new ProjectContract(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getStartDate(),
        entity.getEndDate(),
        sprintContracts,
        eventContracts,
        milestoneContracts,
        deadlineContracts);
  }
}
