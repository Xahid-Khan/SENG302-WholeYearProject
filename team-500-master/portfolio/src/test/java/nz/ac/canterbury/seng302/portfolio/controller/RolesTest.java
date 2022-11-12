package nz.ac.canterbury.seng302.portfolio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
public class RolesTest {

    private final int projectId = 100;
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserAccountService userAccountService;

    @BeforeEach
    public void beforeEach() {
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.STUDENT)
                .build()
        );
    }

    /**
     * This test bock test if the user can access the remove project API end-point.
     *
     * @throws Exception
     */
    @Test
    public void removeProject() throws Exception {
        var apiPath = "/api/v1/projects/" + projectId;
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

        this.mockMvc.perform(delete(apiPath))
                                .andExpect(status().isForbidden())
                                .andReturn();

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.TEACHER)
                .build()
        );
        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(-100)
              .setUsername("testing")
              .addRoles(UserRole.COURSE_ADMINISTRATOR)
              .build()
        );
        this.mockMvc.perform(delete(apiPath))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(-100)
              .setUsername("testing")
              .addAllRoles(List.of(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
              .build()
        );
        this.mockMvc.perform(delete("/api/v1/projects/some_project"))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addAllRoles(List.of(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
                .build()
        );
        this.mockMvc.perform(delete("/api/v1/projects/123456"))
                .andExpect(status().isBadRequest());
    }


    /**
     * This test bock test if the user can access the View ALl project API end-point.
     *
     * @throws Exception
     */
    @Test
    public void viewAllProjects() throws Exception {
        var apiPath = "/api/v1/projects/";
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

        this.mockMvc.perform(get(apiPath))
                .andExpect(status().isOk())
                .andReturn();

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.TEACHER)
                .build()
        );
        this.mockMvc.perform(get(apiPath))
                .andExpect(status().isOk());

        AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.COURSE_ADMINISTRATOR)
                .build()
        );
        this.mockMvc.perform(get(apiPath))
                .andExpect(status().isOk());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addAllRoles(List.of(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
                .build()
        );
        this.mockMvc.perform(get("/api/v1/projects/some_project"))
                .andExpect(status().isNotFound());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addAllRoles(List.of(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
                .build()
        );
        this.mockMvc.perform(get("/api/v1/projects/123456"))
                .andExpect(status().isNotFound());
    }

    /**
     * This test bock test if the user can access the update project API end-point.
     *
     * @throws Exception
     */
    @Test
    public void updateProject() throws Exception {
        var apiPath = "/api/v1/projects/" + projectId;
        var body = """
                {
                    "name": "NewName Project",
                    "description": "Updated project details",
                    "startDate": "2023-01-01T10:00:00.00Z",
                    "endDate": "2023-01-01T10:00:00.00Z"
                }
                """;
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

        this.mockMvc.perform(put(apiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andReturn();

        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.TEACHER)
                .build()
        );
        this.mockMvc.perform(put(apiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addRoles(UserRole.COURSE_ADMINISTRATOR)
                .build()
        );
        this.mockMvc.perform(put(apiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addAllRoles(List.of(UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
                .build()
        );
        this.mockMvc.perform(put("/api/v1/projects/some_project"))
                .andExpect(status().isBadRequest());

        AuthorisationParamsHelper.setParams(
            "role",
            Arrays.asList(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR)
        );
        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
            UserResponse.newBuilder()
                .setId(-100)
                .setUsername("testing")
                .addAllRoles(List.of(UserRole.STUDENT, UserRole.TEACHER, UserRole.COURSE_ADMINISTRATOR))
                .build()
        );
        this.mockMvc.perform(put("/api/v1/projects/123456"))
                .andExpect(status().isBadRequest());
    }
}
