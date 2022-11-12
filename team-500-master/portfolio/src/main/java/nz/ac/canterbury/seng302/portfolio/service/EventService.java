package nz.ac.canterbury.seng302.portfolio.service;

import java.util.NoSuchElementException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import nz.ac.canterbury.seng302.portfolio.mapping.EventMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.EventContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseEventContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.EventEntity;
import nz.ac.canterbury.seng302.portfolio.repository.EventRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventService {
  @Autowired private ProjectRepository projectRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private EventMapper eventMapper;

  @PersistenceContext private EntityManager entityManager;

  /**
   * Get the event with the event ID
   *
   * @param eventId The event ID
   * @throws IllegalArgumentException If the event ID is invalid
   * @return The event contract with the event ID
   */
  public EventContract get(String eventId) {
    var event = eventRepository.findById(eventId).orElseThrow();
    return eventMapper.toContract(event);
  }

  /**
   * Creates an event within a project and puts it in a sprint if it falls within the sprint's start
   * and end dates.
   *
   * @param projectId the project's ID
   * @param event a base event contract
   * @return a full event contract
   */
  public EventContract createEvent(String projectId, BaseEventContract event) {

    var project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
    var entity = eventMapper.toEntity(event);
    project.addEvent(entity);
    eventRepository.save(entity);
    projectRepository.save(project);

    return eventMapper.toContract(entity);
  }

  /**
   * Deletes an event, including removing it from its parent project.
   *
   * @param eventId to delete
   * @throws NoSuchElementException if the id is invalid
   */
  public void delete(String eventId) {
    var event =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new NoSuchElementException("Invalid event ID"));
    var project = event.getProject();

    eventRepository.deleteById(eventId);
    project.removeEvent(event);
  }

  /**
   * Updates an event using the EventContract provided
   *
   * @param eventId to update
   * @param event to update, with the new values
   */
  public void update(String eventId, BaseEventContract event) {

    EventEntity eventEntity =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new NoSuchElementException("Invalid event ID"));

    eventEntity.setName(event.name());
    eventEntity.setDescription(event.description());
    eventEntity.setStartDate(event.startDate());
    eventEntity.setEndDate(event.endDate());

    eventRepository.save(eventEntity);
    entityManager.clear();
  }
}
