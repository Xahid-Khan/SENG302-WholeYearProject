package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.GroupRepositoryContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.GroupRepositoryEntity;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.GroupRepositoryService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This class tests the GroupRepositoryController functionality
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
class GroupRepositoryControllerTest {

  private final int validUserId = 3;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private GroupRepositoryController controller;
  @MockBean
  private GroupRepositoryService groupRepositoryService;
  @MockBean
  private UserAccountService userAccountService;
  @MockBean
  private GroupsClientService groupsClientService;
  @MockBean
  private AuthStateService authStateService;
  private int requestedID = 1;
  private int requestedRepositoryID = 1;
  private String requestedToken = "TOKEN";
  private String requestedIDString = "1";
  private GroupRepositoryEntity repoEntity1;
  private GroupRepositoryContract repoContract1;

  /**
   * Mocks the authentication service to return a valid student
   */
  @BeforeEach
  void setupBeforeEach() {
    Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
        UserResponse.newBuilder()
            .setId(validUserId)
            .setUsername("testing")
            .build()
    );
    Mockito.when(authStateService.getId(any(PortfolioPrincipal.class))).thenReturn(3);
    AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

  }

  /**
   * This test makes sure that controller is loaded and running.
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void contextLoads() throws Exception {
    Assertions.assertNotNull(controller);
  }

  /**
   * Get a repo
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void getValidRepoAndExpectPass() throws Exception {
    Mockito.when(groupRepositoryService.get(requestedIDString))
        .thenReturn(new GroupRepositoryContract(1, 1, "TOKEN", "", ""));

    var result = mockMvc.perform(get("/groups/repository/" + requestedIDString + "/"))
        .andExpect(status().isOk())
        .andReturn();
    var response = (new JSONObject(result.getResponse().getContentAsString()));

    //Checks that the response id matches the requested id
    Assertions.assertNotNull(response);
    Assertions.assertEquals(requestedIDString, response.get("groupId").toString());
  }

  /**
   * Get an invalid repo
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void getInvalidRepoAndExpectFail() throws Exception {
    Mockito.when(groupRepositoryService.get(requestedIDString)).thenReturn(null);

    var result = mockMvc.perform(get("/groups/repository/1"))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  /**
   * Add a repo with a valid id
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void addValidRepoAndExpectPass() throws Exception {
    Mockito.when(groupRepositoryService.add(requestedID))
        .thenReturn(new GroupRepositoryContract(1, -1, "No token", "", ""));

    mockMvc.perform(post("/groups/add_repository/")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1"                           
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  /**
   * Add a repo with a valid id twice
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void addValidRepoTwiceAndExpectFail() throws Exception {
    Mockito.when(groupRepositoryService.add(requestedID))
        .thenReturn(new GroupRepositoryContract(1, -1, "No token", "", "")).thenReturn(null);

    mockMvc.perform(post("/groups/add_repository/")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1"                           
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    //post again
    mockMvc.perform(post("/groups/repository")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1"                           
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andReturn();
  }

  /**
   * Add a repo with a string as the id
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void addInvalidRepoAndExpectFail() throws Exception {
    Mockito.when(groupRepositoryService.add(requestedID))
        .thenReturn(new GroupRepositoryContract(1, -1, "No token", "", ""));

    mockMvc.perform(post("/groups/add_repository/")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "abc"                           
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andReturn();
  }

  /**
   * Delete a repo with a valid id
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void deleteValidRepoAndExpectPass() throws Exception {
    Mockito.when(groupRepositoryService.delete(requestedID)).thenReturn(true);

    mockMvc.perform(delete("/groups/delete_repository/" + requestedIDString + "/"))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  /**
   * Delete a repo that does not exist
   */
  @Test
  void deleteInvalidRepoAndExpectFail() throws Exception {
    Mockito.when(groupRepositoryService.delete(requestedID)).thenReturn(false);

    mockMvc.perform(delete("/groups/delete_repository/" + requestedIDString + "/"))
        .andExpect(status().is5xxServerError())
        .andReturn();
  }


  /**
   * Update a repo which exists
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void updateValidRepoAndExpectPass() throws Exception {
    //mocks service finding repo
    Mockito.when(groupRepositoryService.update(anyInt(), anyInt(), anyString(), anyString()))
        .thenReturn(true);

    mockMvc.perform(put("/groups/update_repository")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1",
                    "repositoryId" : "1",
                    "token" : "TOKEN",
                    "alias" : "a",
                    "longName" : ""
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }

  /**
   * Update a repo which does not exist
   *
   * @throws Exception if mockMvc fails
   */
  @Test
  void updateInvalidRepoAndExpectFail() throws Exception {
    //mocks service not finding repo
    Mockito.when(
            groupRepositoryService.update(requestedID, requestedRepositoryID, requestedToken, ""))
        .thenReturn(false);

    mockMvc.perform(put("/groups/update_repository")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1",
                    "repositoryId" : "1",
                    "token" : "TOKEN",
                    "alias" : "",
                    "longName" : ""
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andReturn();
  }


  /**
   * Tests getting all repository info
   */
  @Test
  void getAllRepoInfoAndExpectPass() throws Exception {
    Mockito.when(groupRepositoryService.getAll()).thenReturn(
        List.of(new GroupRepositoryContract(requestedID, 1, "TOKEN", "", ""),
            new GroupRepositoryContract(2, 2, "TOKEN", "", "")));

    var result = mockMvc.perform(get("/groups/all_repository/"))
        .andExpect(status().isOk())
        .andReturn();
    var response = (new JSONArray(result.getResponse().getContentAsString()));

    //Checks that the response id matches the requested id
    Assertions.assertNotNull(response);
    Assertions.assertEquals(2, response.length());
    Assertions.assertEquals(response.getJSONObject(0).get("groupId").toString(), requestedIDString);
  }
}





