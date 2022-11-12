package nz.ac.canterbury.seng302.portfolio.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.EventContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.EventEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.repository.EventRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Class to test the EventController.class
 * This class handles all functionality to do with events
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class EventControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAccountService userAccountService;

    /**
     * Clear both repositories and set the role of the user to teacher, so they can use crud functionality
     * without permission issues. Clean slate on repositories allows for no unintended bugs with old
     * test objects in the repositories
     */
    @BeforeEach
    public void beforeEach() {
        eventRepository.deleteAll();
        projectRepository.deleteAll();

        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.TEACHER)
                .build()
        );

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
    }

    /**
     * Test getting an event that does not exist, and with an invalid ID
     * @throws Exception thrown due to invalid ID
     */
    @Test
    public void getWithInvalidId() throws Exception {
        this.mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isNotFound());

        this.mockMvc.perform(get("/api/v1/events/this isn't a valid ID"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Test getting an event with a valid ID. First adds event and relevant project to database, then fetches
     * the event and checks all the details are correct
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getById() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var event = new EventEntity("test event", "test description", Instant.ofEpochSecond(120), Instant.ofEpochSecond(360));
        project.addEvent(event);
        projectRepository.save(project);
        eventRepository.save(event);

        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/events/%s", event.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, EventContract.class);

        assertEquals(1, decodedResponse.orderNumber());
        assertEquals("test event" , decodedResponse.name());
        assertEquals("test description", decodedResponse.description());
        assertEquals(Instant.ofEpochSecond(120), decodedResponse.startDate());
        assertEquals(Instant.ofEpochSecond(360), decodedResponse.endDate());
    }

    /**
     * Test getting all events related to a project. First adds events and relevant project to database, then fetches
     * the event and checks all the details are correct
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getManyByProjectId() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var event = new EventEntity("event", "test description", Instant.ofEpochSecond(120), Instant.ofEpochSecond(360));
        var event2 = new EventEntity("testevent", "test description 2", Instant.ofEpochSecond(420), Instant.ofEpochSecond(480));
        project.addEvent(event);
        project.addEvent(event2);
        projectRepository.save(project);
        eventRepository.save(event);
        eventRepository.save(event2);

        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/projects/%s/events", project.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, new TypeReference<ArrayList<EventContract>>(){});

        var receivedEvent1 = decodedResponse.get(0);
        var receivedEvent2 = decodedResponse.get(1);

        assertEquals(1, receivedEvent1.orderNumber());
        assertEquals("event" , receivedEvent1.name());
        assertEquals("test description", receivedEvent1.description());
        assertEquals(Instant.ofEpochSecond(120), receivedEvent1.startDate());
        assertEquals(Instant.ofEpochSecond(360), receivedEvent1.endDate());

        assertEquals(2, receivedEvent2.orderNumber());
        assertEquals("testevent" , receivedEvent2.name());
        assertEquals("test description 2", receivedEvent2.description());
        assertEquals(Instant.ofEpochSecond(420), receivedEvent2.startDate());
        assertEquals(Instant.ofEpochSecond(480), receivedEvent2.endDate());
    }

    /**
     * Tries to get all events from a project that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getManyByNonExistentProjectId() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        this.mockMvc.perform(
                        get("/api/v1/projects/fake_project/events")
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Creates new valid event and posts it in order to add it to the database. Ensures all details are
     * added correctly from the response.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNew() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        projectRepository.save(project);

        Mockito.when(userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true)).thenReturn(PaginatedUsersResponse.newBuilder().build());

        var apiPath = String.format("/api/v1/projects/%s/events", project.getId());
        var body = """
            {
                "name": "test event",
                "startDate": "2023-01-01T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z",
                "description": "test description"
            }
            """;

        var result = this.mockMvc.perform(
                        post(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, EventContract.class);

        assertEquals("test event" , decodedResponse.name());
        assertEquals("test description", decodedResponse.description());
        assertEquals(Instant.parse("2023-01-01T10:00:00.00Z"), decodedResponse.startDate());
        assertEquals(Instant.parse("2023-01-15T10:00:00.00Z"), decodedResponse.endDate());
    }

    /**
     * Tries to create an event with an invalid project that does not exist.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNewInvalidProject() throws Exception {
        var body = """
            {
                "name": "test event",
                "startDate": "2023-01-01T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z",
                "description": "test description"
            }
            """;

        this.mockMvc.perform(
                        post("/api/v1/projects/fake_project/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Creates a new valid event with no description, as description is not required
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNewNoDescription() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        projectRepository.save(project);

        Mockito.when(userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true)).thenReturn(PaginatedUsersResponse.newBuilder().build());

        var apiPath = String.format("/api/v1/projects/%s/events", project.getId());
        var body = """
            {
                "name": "testevent",
                "startDate": "2023-01-01T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z"
            }
            """;

        var result = this.mockMvc.perform(
                        post(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, EventContract.class);

        assertEquals("testevent" , decodedResponse.name());
        assertNull(decodedResponse.description());
        assertEquals(Instant.parse("2023-01-01T10:00:00.00Z"), decodedResponse.startDate());
        assertEquals(Instant.parse("2023-01-15T10:00:00.00Z"), decodedResponse.endDate());
    }

    /**
     * Tries to update an event that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void updateInvalidEvent() throws Exception {
        var body = """
            {
                "name": "test event",
                "startDate": "2023-01-01T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z",
                "description": "test description"
            }
            """;

        this.mockMvc.perform(
                        put("/api/v1/events/fake_event")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Updates an event with valid information. Tests to ensure all fields are correctly updated
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void updateValidEvent() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        var event = new EventEntity("event", "pre-test description", Instant.parse("2023-01-01T10:15:30.00Z"), Instant.parse("2023-01-03T10:15:30.00Z"));
        project.addEvent(event);
        projectRepository.save(project);
        eventRepository.save(event);
        String projectId = project.getId();
        String eventId = event.getId();
        var apiPath = String.format("/api/v1/events/%s", event.getId());
        var body = String.format("""
            {
                "projectId": "%s",
                "eventId": "%s",
                "name": "postedittestevent",
                "startDate": "2023-01-04T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z"
            }
            """, projectId, eventId);
        this.mockMvc.perform(
                        put(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());

        // Check that update was persisted.
        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/events/%s", event.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, EventContract.class);

        assertEquals("postedittestevent" , decodedResponse.name());
        assertNull(decodedResponse.description());
        assertEquals(Instant.parse("2023-01-04T10:00:00.00Z"), decodedResponse.startDate());
        assertEquals(Instant.parse("2023-01-15T10:00:00.00Z"), decodedResponse.endDate());
    }

    /**
     * Tries to delete an event that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void deleteInvalidEvent() throws Exception {
        this.mockMvc.perform(delete("/api/v1/events/fake_event"))
                .andExpect(status().isNotFound());
    }

    /**
     * Deletes an event from the database, ensures that the event has been deleted
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void deleteValidEvent() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var event = new EventEntity("event", "pre-test description", Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        project.addEvent(event);
        projectRepository.save(project);
        eventRepository.save(event);

        var apiPath = String.format("/api/v1/events/%s", event.getId());

        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isNoContent());

        // Check that update was persisted.
        this.mockMvc.perform(get(apiPath))
                .andExpect(status().isNotFound());
    }

    /**
     * Tries to create a valid event with invalid permissions, as the user needs to be a teacher or
     * course admin to create.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void tryCreateNewAsStudent() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.STUDENT)
                .build()
        );
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        projectRepository.save(project);

        var apiPath = String.format("/api/v1/projects/%s/events", project.getId());
        var body = """
            {
                "name": "test event",
                "startDate": "2023-01-01T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z"
            }
            """;

        this.mockMvc.perform(
                        post(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Tries to update a valid event with invalid permissions, as the user needs to be a teacher or
     * course admin to update.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void tryUpdateValidEventAsStudent() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.STUDENT)
                .build()
        );
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        var event = new EventEntity("event", "pre-test description", Instant.parse("2023-01-01T10:15:30.00Z"), Instant.parse("2023-01-03T10:15:30.00Z"));
        project.addEvent(event);
        projectRepository.save(project);
        eventRepository.save(event);
        String projectId = project.getId();
        String eventId = event.getId();
        var apiPath = String.format("/api/v1/events/%s", event.getId());
        var body = String.format("""
            {
                "projectId": "%s",
                "eventId": "%s",
                "name": "post-edit test event",
                "startDate": "2023-01-04T10:00:00.00Z",
                "endDate": "2023-01-15T10:00:00.00Z"
            }
            """, projectId, eventId);
        this.mockMvc.perform(
                        put(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());

    }

    /**
     * Tries to delete a valid event with invalid permissions, as the user needs to be a teacher or
     * course admin to delete.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void tryDeleteEventAsStudent() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.STUDENT)
                .build()
        );
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var event = new EventEntity("testevent", "pre-test description", Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        project.addEvent(event);
        projectRepository.save(project);
        eventRepository.save(event);

        var apiPath = String.format("/api/v1/events/%s", event.getId());

        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isForbidden());
    }

}