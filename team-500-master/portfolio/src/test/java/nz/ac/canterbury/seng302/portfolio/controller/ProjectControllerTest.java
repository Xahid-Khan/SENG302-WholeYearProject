package nz.ac.canterbury.seng302.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.model.contract.ProjectContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.SprintEntity;
import nz.ac.canterbury.seng302.portfolio.repository.ProjectRepository;
import nz.ac.canterbury.seng302.portfolio.repository.SprintRepository;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc ;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintRepository sprintRepository;

    private String projectId;

    @MockBean
    private UserAccountService userAccountService;


    /**
     * This method will make sure the database is reset prior to testing,
     * and it will create a mock project and sprint and save it in the database
     * Assign the Project ID of newly created project to the variable projectId.
     */
    @BeforeEach
    public void beforeEach() {
        projectRepository.deleteAll();
        var project1 = new ProjectEntity(
                "Project 1",
                "This is a test Project",
                Instant.EPOCH,
                Instant.parse("2022-03-03T10:15:30.00Z")
        );

        var sprint = new SprintEntity("New sprint", "My New Sprint Description", Instant.parse("2022-03-03T10:15:30.00Z"), Instant.parse("2023-03-03T10:15:30.00Z"), "#fff");
        project1.addSprint(sprint);

        projectRepository.save(project1);
        sprintRepository.save(sprint);

        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.TEACHER)
                .build()
        );

        projectId = project1.getId();
        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
    }


    /**
     * This test method try to check all the possible API endpoints for GET method.
     * Cases:
     *      All the projects and sprints
     *      A specific project using project ID
     *      Trying to get an project by providing an invalid ID
     *      GET method with invalid url/ID.
     * @throws Exception this method will throw an exception if anyone of these test cases fails.
     */
    @Test
    public void getProjects() throws Exception {
//        GetAuthorizationParams param1 = new GetAuthorizationParams("role", "TEACHER");
        this.mockMvc.perform(get("/api/v1/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        var result = this.mockMvc.perform(get("/api/v1/projects/" + projectId))
                        .andExpect(status().isOk())
                        .andReturn();

        this.mockMvc.perform(get("/api/v1/projects/3333"))
                .andExpect(status().isNotFound());

        this.mockMvc.perform(get("/api/v1/projects/Some ID"))
                .andExpect(status().is4xxClientError());

        var projectResponse = mapper.readValue(result.getResponse().getContentAsString(), ProjectContract.class);
        assertEquals(projectResponse.sprints().size(), 1);

    }

    /**
     * This test method try to check all the possible API endpoints for POST method.
     * Creates a new project and saves it in database, then check all the fields in database.
     * @throws Exception this method will throw an exception if anyone of these test cases fails.
     */
    @Test
    public void createProject() throws Exception {
//        GetAuthorizationParams param1 = new GetAuthorizationParams("role", "TEACHER");
        var apiPath = "/api/v1/projects";
        var body = """
                {
                    "name" : "Project 2",
                    "description" : "NewProject",
                    "startDate": "2022-01-01T10:00:00.00Z",
                    "endDate": "2023-01-01T10:00:00.00Z"
                }
                """;

        var result = this.mockMvc.perform(
                post(apiPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();


        var replyString = result.getResponse().getContentAsString();
        var responseDecode = mapper.readValue(replyString, ProjectContract.class);

        assertEquals("Project 2", responseDecode.name());
        assertEquals("NewProject", responseDecode.description());
        assertEquals(Instant.parse("2022-01-01T10:00:00Z"), responseDecode.startDate());
        assertEquals(Instant.parse("2023-01-01T10:00:00.00Z"), responseDecode.endDate());

    }

    /**
     * This test method try to check all the possible API endpoints for DELETE method.
     * Cases:
     *      Delete a specific project using project ID
     *      Trying to get a deleted project using project ID
     *      Trying to delete a project which is already deleted
     *      Trying to delete a project with invalid ID.
     * @throws Exception this method will throw an exception if anyone of these test cases fails.
     */
    @Test
    public void removeProject() throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        var apiPath = "/api/v1/projects/" + projectId;

        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isNoContent());

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        this.mockMvc.perform(delete("/api/v1/projects/some_project"))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        this.mockMvc.perform(delete("/api/v1/projects/123456"))
                .andExpect(status().isBadRequest());
    }

    /**
     * This test method try to check all the possible API endpoints for PUT method.
     * updated a project partially and then checked all the correct fields are updated
     * and rest remains unchanged. Project ID is Unchanged as well.
     * @throws Exception this method will throw an exception if anyone of these test cases fails.
     */
    @Test
    public void updateProject() throws Exception {
        var apiPath = "/api/v1/projects/" + projectId;
        var body = """
                {
                    "name": "NewName Project",
                    "description": "Updated project details",
                    "startDate": "2022-01-01T10:00:00.00Z",
                    "endDate": "2024-01-01T10:00:00.00Z"
                }
                """;

        this.mockMvc.perform(put(apiPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk());

        var updatedProject = this.mockMvc.perform(get(apiPath)
                    .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();

        var projectResponse = mapper.readValue(updatedProject.getResponse().getContentAsString(), ProjectContract.class);

        assertEquals("NewName Project", projectResponse.name());
        assertEquals("Updated project details", projectResponse.description());
        assertEquals(Instant.parse("2022-01-01T10:00:00Z"), projectResponse.startDate());
        assertEquals(Instant.parse("2024-01-01T10:00:00.00Z"), projectResponse.endDate());
        assertEquals(projectId, projectResponse.id());
    }

}
