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
import nz.ac.canterbury.seng302.portfolio.model.contract.MilestoneContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.MilestoneEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.repository.MilestoneRepository;
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
 * Class to test the MilestoneController.class
 * This class handles all functionality to do with milestones
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class MilestoneControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MilestoneRepository milestoneRepository;

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
        milestoneRepository.deleteAll();
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
     * Test getting a milestone that does not exist, and with an invalid ID
     * @throws Exception thrown due to invalid ID
     */
    @Test
    public void getWithInvalidId() throws Exception {
        this.mockMvc.perform(get("/api/v1/milestones/1"))
                .andExpect(status().isNotFound());

        this.mockMvc.perform(get("/api/v1/milestones/this isn't a valid ID"))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Test getting an milestone with a valid ID. First adds milestone and relevant project to database, then fetches
     * the milestone and checks all the details are correct
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getById() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var milestone = new MilestoneEntity("testmilestone", "test description", Instant.ofEpochSecond(120));
        project.addMilestone(milestone);
        projectRepository.save(project);
        milestoneRepository.save(milestone);

        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/milestones/%s", milestone.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, MilestoneContract.class);

        assertEquals(1, decodedResponse.orderNumber());
        assertEquals("testmilestone" , decodedResponse.name());
        assertEquals("test description", decodedResponse.description());
        assertEquals(Instant.ofEpochSecond(120), decodedResponse.startDate());
    }

    /**
     * Test getting all milestones related to a project. First adds milestones and relevant project to database, then fetches
     * the milestone and checks all the details are correct
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getManyByProjectId() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var milestone = new MilestoneEntity("milestone", "test description", Instant.ofEpochSecond(120));
        var milestone2 = new MilestoneEntity("testmilestone", "test description 2", Instant.ofEpochSecond(420));
        project.addMilestone(milestone);
        project.addMilestone(milestone2);
        projectRepository.save(project);
        milestoneRepository.save(milestone);
        milestoneRepository.save(milestone2);

        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/projects/%s/milestones", project.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, new TypeReference<ArrayList<MilestoneContract>>(){});

        var receivedMilestone1 = decodedResponse.get(0);
        var receivedMilestone2 = decodedResponse.get(1);

        assertEquals(1, receivedMilestone1.orderNumber());
        assertEquals("milestone" , receivedMilestone1.name());
        assertEquals("test description", receivedMilestone1.description());
        assertEquals(Instant.ofEpochSecond(120), receivedMilestone1.startDate());

        assertEquals(2, receivedMilestone2.orderNumber());
        assertEquals("testmilestone" , receivedMilestone2.name());
        assertEquals("test description 2", receivedMilestone2.description());
        assertEquals(Instant.ofEpochSecond(420), receivedMilestone2.startDate());
    }

    /**
     * Tries to get all milestones from a project that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void getManyByNonExistentProjectId() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        var result = this.mockMvc.perform(
                        get("/api/v1/projects/fake_project/milestones")
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Creates new valid milestone and posts it in order to add it to the database. Ensures all details are
     * added correctly from the response.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNew() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        projectRepository.save(project);

        Mockito.when(userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true)).thenReturn(PaginatedUsersResponse.newBuilder().build());

        var apiPath = String.format("/api/v1/projects/%s/milestones", project.getId());
        var body = """
            {
                "name": "testmilestone",
                "startDate": "2023-01-01T10:00:00.00Z",
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
        var decodedResponse = objectMapper.readValue(stringContent, MilestoneContract.class);

        assertEquals("testmilestone" , decodedResponse.name());
        assertEquals("test description", decodedResponse.description());
        assertEquals(Instant.parse("2023-01-01T10:00:00.00Z"), decodedResponse.startDate());
    }

    /**
     * Tries to create an milestone with an invalid project that does not exist.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNewInvalidProject() throws Exception {
        var body = """
            {
                "name": "testmilestone",
                "startDate": "2023-01-01T10:00:00.00Z",
                "description": "test description"
            }
            """;

        this.mockMvc.perform(
                        post("/api/v1/projects/fake_project/milestones")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound());
    }

    /**
     * Creates a new valid milestone with no description, as description is not required
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void createNewNoDescription() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        projectRepository.save(project);

        Mockito.when(userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true)).thenReturn(PaginatedUsersResponse.newBuilder().build());

        var apiPath = String.format("/api/v1/projects/%s/milestones", project.getId());
        var body = """
            {
                "name": "testmilestone",
                "startDate": "2023-01-01T10:00:00.00Z"
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
        var decodedResponse = objectMapper.readValue(stringContent, MilestoneContract.class);

        assertEquals("testmilestone" , decodedResponse.name());
        assertNull(decodedResponse.description());
        assertEquals(Instant.parse("2023-01-01T10:00:00.00Z"), decodedResponse.startDate());
    }

    /**
     * Tries to update a milestone that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void updateInvalidMilestone() throws Exception {
        var body = """
            {
                "name": "testmilestone",
                "startDate": "2023-01-01T10:00:00.00Z"
                "description": "test description"
            }
            """;

        this.mockMvc.perform(
                        put("/api/v1/milestones/fake_milestone")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().is4xxClientError());
    }

    /**
     * Updates an milestone with valid information. Tests to ensure all fields are correctly updated
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void updateValidMilestone() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        var milestone = new MilestoneEntity("preedittestmilestone", "pre-test description", Instant.parse("2023-01-02T10:00:00.00Z"));
        project.addMilestone(milestone);
        projectRepository.save(project);
        milestoneRepository.save(milestone);
        String projectId = project.getId();
        String milestoneId = milestone.getId();
        var apiPath = String.format("/api/v1/milestones/%s", milestone.getId());
        var body = String.format("""
            {
                "projectId": "%s",
                "milestoneId": "%s",
                "name": "postedittestmilestone",
                "startDate": "2023-01-04T10:00:00.00Z"
            }
            """, projectId, milestoneId);
        this.mockMvc.perform(
                        put(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());

        // Check that update was persisted.
        var result = this.mockMvc.perform(
                        get(String.format("/api/v1/milestones/%s", milestone.getId()))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        var stringContent = result.getResponse().getContentAsString();
        var decodedResponse = objectMapper.readValue(stringContent, MilestoneContract.class);

        assertEquals("postedittestmilestone" , decodedResponse.name());
        assertNull(decodedResponse.description());
        assertEquals(Instant.parse("2023-01-04T10:00:00.00Z"), decodedResponse.startDate());
    }

    /**
     * Tries to delete a milestone that does not exist
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void deleteInvalidMilestone() throws Exception {
        this.mockMvc.perform(delete("/api/v1/milestones/fake_milestone"))
                .andExpect(status().isNotFound());
    }

    /**
     * Deletes a milestone from the database, ensures that the milestone has been deleted
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void deleteValidMilestone() throws Exception {
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var milestone = new MilestoneEntity("preedittestmilestone", "pre-test description", Instant.parse("2007-12-03T10:15:30.00Z"));
        project.addMilestone(milestone);
        projectRepository.save(project);
        milestoneRepository.save(milestone);

        var apiPath = String.format("/api/v1/milestones/%s", milestone.getId());

        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isNoContent());

        // Check that update was persisted.
        this.mockMvc.perform(get(apiPath))
                .andExpect(status().isNotFound());
    }

    /**
     * Tries to create a valid milestone with invalid permissions, as the user needs to be a teacher or
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

        var apiPath = String.format("/api/v1/projects/%s/milestones", project.getId());
        var body = """
            {
                "name": "testmilestone",
                "startDate": "2023-01-01T10:00:00.00Z"
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
     * Tries to update a valid milestone with invalid permissions, as the user needs to be a teacher or
     * course admin to update.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void tryUpdateValidMilestoneAsStudent() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(-100)
              .setUsername("testing")
              .addRoles(UserRole.STUDENT)
              .build()
        );
        var project = new ProjectEntity("test project", null, Instant.parse("2022-12-01T10:15:30.00Z"), Instant.parse("2023-01-20T10:15:30.00Z"));
        var milestone = new MilestoneEntity("preedittestmilestone", "pre-test description", Instant.parse("2023-01-03T10:15:30.00Z"));
        project.addMilestone(milestone);
        projectRepository.save(project);
        milestoneRepository.save(milestone);
        String projectId = project.getId();
        String milestoneId = milestone.getId();
        var apiPath = String.format("/api/v1/milestones/%s", milestone.getId());
        var body = String.format("""
            {
                "projectId": "%s",
                "milestoneId": "%s",
                "name": "postedittestmilestone",
                "startDate": "2023-01-04T10:00:00.00Z"
            }
            """, projectId, milestoneId);
        this.mockMvc.perform(
                        put(apiPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());

    }

    /**
     * Tries to delete a valid milestone with invalid permissions, as the user needs to be a teacher or
     * course admin to delete.
     * @throws Exception thrown by method getting object from database
     */
    @Test
    public void tryDeleteMilestoneAsStudent() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
      Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(-100)
              .setUsername("testing")
              .addRoles(UserRole.STUDENT)
              .build()
      );
        var project = new ProjectEntity("test project", null, Instant.EPOCH, Instant.parse("2007-12-03T10:15:30.00Z"));
        var milestone = new MilestoneEntity("preedittestmilestone", "pre-test description", Instant.parse("2007-12-03T10:15:30.00Z"));
        project.addMilestone(milestone);
        projectRepository.save(project);
        milestoneRepository.save(milestone);

        var apiPath = String.format("/api/v1/milestones/%s", milestone.getId());

        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isForbidden());
    }
}
