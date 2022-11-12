package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseEventContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.EventContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.EventEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.repository.EventRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class EventServiceTest {
  @Autowired private ProjectRepository projectRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private EventService eventService;

  @BeforeEach
  public void beforeEach() {
    eventRepository.deleteAll();
    projectRepository.deleteAll();

    AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
  }

  /**
   * Tests creating an event
   *
   * @throws Exception
   */
  @Test
  public void testCreateEvent() {
    // Create a project
    var project =
        new ProjectEntity(
            "testproject",
            null,
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2023-01-20T10:15:30.00Z"));
    projectRepository.save(project);
    // Create an event
    BaseEventContract event =
        new BaseEventContract(
            "test event",
            "testdescription",
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2022-12-02T10:15:30.00Z"));
    EventContract eventContract = eventService.createEvent(project.getId(), event);

    // Check that the event was created
    var events = eventRepository.findAll();
    assert events.iterator().hasNext();

    // Checks the event name
    var event1 = events.iterator().next();
    assertEquals("test event", event1.getName());

    // Tests event was added to project
    var projects = projectRepository.findAll();
    var project1 = projects.iterator().next();
    assertEquals(event1.getId(), project1.getEvents().get(0).getId());
  }

  @Test
  public void testDeleteEvent() {
    // Create a project
    var project =
        new ProjectEntity(
            "testproject",
            null,
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2023-01-20T10:15:30.00Z"));
    projectRepository.save(project);

    // Create an event
    BaseEventContract event =
        new BaseEventContract(
            "test event",
            "testdescription",
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2022-12-02T10:15:30.00Z"));
    eventService.createEvent(project.getId(), event);

    // Check that the event was created
    var events = eventRepository.findAll();
    assertTrue(events.iterator().hasNext());

    // Checks the event name
    var event1 = events.iterator().next();
    assertEquals("test event", event1.getName());

    // Tests event was added to project
    var project1 = projectRepository.findAll().iterator().next();
    assertEquals(event1.getId(), project1.getEvents().get(0).getId());

    // deletes it
    var idToDelete = event1.getId();
    eventService.delete(idToDelete);

    // Check the event is deleted
    assertEquals(0, projectRepository.findAll().iterator().next().getEvents().size());
  }

  @Test
  public void testUpdateEvent() {
    // Create a project
    var project =
        new ProjectEntity(
            "testproject",
            null,
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2023-01-20T10:15:30.00Z"));
    projectRepository.save(project);

    // Create an event
    BaseEventContract event =
        new BaseEventContract(
            "test event",
            "testdescription",
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2022-12-02T10:15:30.00Z"));
    eventService.createEvent(project.getId(), event);

    // Check that the event was created
    var events = eventRepository.findAll();
    assertTrue(events.iterator().hasNext());

    // Checks the event name
    var event1 = events.iterator().next();
    assertEquals("test event", event1.getName());

    // Tests event was added to project
    var project1 = projectRepository.findAll().iterator().next();
    assertEquals(event1.getId(), project1.getEvents().get(0).getId());

    // update it
    var idToUpdate = eventRepository.findAll().iterator().next().getId();
    BaseEventContract event2 =
        new BaseEventContract(
            "test event two",
            "testdescription2",
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2022-12-02T10:15:30.00Z"));
    eventService.update(idToUpdate, event2);

    // Check the event is still there
    assertEquals(1, projectRepository.findAll().iterator().next().getEvents().size());

    EventEntity updatedEvent = eventRepository.findById(idToUpdate).orElse(null);
    assertNotNull(updatedEvent);
    assertEquals(event2.description(), updatedEvent.getDescription());
    assertEquals(event2.name(), updatedEvent.getName());
  }

  @Test
  public void testGetEvent() {
    // Create a project
    var project =
        new ProjectEntity(
            "testproject",
            null,
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2023-01-20T10:15:30.00Z"));
    projectRepository.save(project);

    // Create an event
    BaseEventContract event =
        new BaseEventContract(
            "test event",
            "testdescription",
            Instant.parse("2022-12-01T10:15:30.00Z"),
            Instant.parse("2022-12-02T10:15:30.00Z"));
    eventService.createEvent(project.getId(), event);

    // Check that the event was created
    var events = eventRepository.findAll();
    assertTrue(events.iterator().hasNext());

    // Checks the event name
    var event1 = events.iterator().next();
    assertEquals("test event", event1.getName());

    // Tests event was added to project
    var project1 = projectRepository.findAll().iterator().next();
    assertEquals(event1.getId(), project1.getEvents().get(0).getId());

    // get it
    var idToUpdate = eventRepository.findAll().iterator().next().getId();
    EventContract retrievedEvent = eventService.get(idToUpdate);

    assertNotNull(retrievedEvent);

    // Check the event is still there
    assertEquals(event.name(), retrievedEvent.name());
    assertEquals(event.description(), retrievedEvent.description());
    assertEquals(event.startDate(), retrievedEvent.startDate());
    assertEquals(event.endDate(), retrievedEvent.endDate());
  }
}
