package nz.ac.canterbury.seng302.portfolio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.CommentContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.CommentModel;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.CommentService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
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

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWebTestClient
class CommentControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private CommentService commentService;
  @MockBean
  private UserAccountService userAccountService;
  @MockBean
  private AuthStateService authStateService;

  private int validUserId = 3;
  private List<Map<String, Object>> comments;

  @BeforeEach
  void setup() {
    Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
        UserResponse.newBuilder()
            .setId(validUserId)
            .setUsername("NewTestUser")
            .addAllRoles(List.of(UserRole.STUDENT))
            .build()
    );
    Mockito.when(authStateService.getId(any(PortfolioPrincipal.class))).thenReturn(validUserId);
    AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

    comments = new ArrayList<>();

    Map<String, Object> commentObject = new HashMap<>();
    commentObject.put("commentId", 1);
    commentObject.put("userId", 3);
    commentObject.put("name", "NewTestUser");
    commentObject.put("time", new Date().getTime());
    commentObject.put("content", "This is a new Comment Test");
    comments.add(commentObject);

    Map<String, Object> commentObject1 = new HashMap<>();
    commentObject.put("commentId", 2);
    commentObject.put("userId", 4);
    commentObject.put("name", "NewTestUser");
    commentObject.put("time", new Date().getTime());
    commentObject.put("content", "This is a new Comment Test");
    comments.add(commentObject1);

    Map<String, Object> commentObject2 = new HashMap<>();
    commentObject.put("commentId", 3);
    commentObject.put("userId", 3);
    commentObject.put("name", "NewTestUser");
    commentObject.put("time", new Date().getTime());
    commentObject.put("content", "This is a new Comment Test");
    comments.add(commentObject2);

  }

  /**
   * This function tests that the end-user can get the comments for a given post
   *
   * @throws Exception
   */
  @Test
  void getAllCommentsForAGroupPostExpectPass() throws Exception {
    Mockito.when(commentService.getCommentsForThePostAsJson(any(int.class)))
        .thenReturn(comments);

    var result = mockMvc.perform(get("/group_feed/comments/1"))
        .andExpect(status().isOk())
        .andReturn();
    var data = result.getResponse().getContentAsString();
    Assertions.assertNotNull(data);
  }

  /**
   * This function tests that the end-user can get the comments for a given post but there are no
   * comments for that group
   *
   * @throws Exception
   */
  @Test
  void getAllCommentsForAGroupPostExpectNotFound() throws Exception {
    Mockito.when(commentService.getCommentsForThePostAsJson(any(int.class)))
        .thenReturn(new ArrayList<>());

    mockMvc.perform(get("/group_feed/comments/1"))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  /**
   * This method tests the creating of new comment with all the valid entries.
   *
   * @throws Exception
   */
  @Test
  void addCommentsToAPostExpectPass() throws Exception {
    Mockito.when(commentService.addNewCommentsToPost(any(CommentContract.class)))
        .thenReturn(new CommentModel(1, 3, "This is a new Test Comment"));

    var result = mockMvc.perform(post("/group_feed/add_comment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                  "postId" : 1,
                  "userId" : 3,
                  "comment": "This is a new Test Comment"
                  }
                """))
        .andExpect(status().isCreated())
        .andReturn();
    Assertions.assertNotNull(result.getResponse().getContentAsString());
  }

  /**
   * This method tests the creation of a new comment where the userId provided in the contract
   * doesn't match the userId of the authenticated user, and it returns FORBIDDEN as expected.
   *
   * @throws Exception
   */
  @Test
  void addCommentsToAPostButUserIdDoesNotMatchExpectFail() throws Exception {
    Mockito.when(commentService.addNewCommentsToPost(any(CommentContract.class)))
        .thenReturn(new CommentModel(1, 5, "This is a new Test Comment"));

    mockMvc.perform(post("/group_feed/add_comment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                  "postId" : 1,
                  "userId" : 5,
                  "comment": "This is a new Test Comment"
                  }
                """))
        .andExpect(status().isForbidden())
        .andReturn();
  }

  /**
   * This method tests the deletion of the comment using the commentId.
   *
   * @throws Exception
   */
  @Test
  void deleteACommentUsingCommentIdExpectPass() throws Exception {
    CommentModel comment = new CommentModel(1, 3, "This is a new Test Comment");
    Mockito.when(commentService.getCommentById(any(int.class))).thenReturn(Optional.of(comment));

    Mockito.when(commentService.deleteCommentById(any(int.class))).thenReturn(true);

    mockMvc.perform(delete("/group_feed/delete_comment/1"))
        .andExpect(status().isOk());
  }

  /**
   * This method tests the deletion of the comment using the commentId. Where userId of
   * authenticated User and the userId of comment doesn't match.
   *
   * @throws Exception
   */
  @Test
  void deleteACommentUsingCommentIdExpectUnauthorised() throws Exception {
    CommentModel comment = new CommentModel(1, 5, "This is a new Test Comment");
    Mockito.when(commentService.getCommentById(any(int.class))).thenReturn(Optional.of(comment));

    Mockito.when(commentService.deleteCommentById(any(int.class))).thenReturn(true);

    mockMvc.perform(delete("/group_feed/delete_comment/1"))
        .andExpect(status().isUnauthorized());
  }

  /**
   * This method tests the deletion of the comment using the commentId. Where userId of
   * authenticated User and the userId of comment doesn't match, but the user Is Teacher.
   *
   * @throws Exception
   */
  @Test
  void deleteACommentUsingCommentIdUserIsTeacherExpectPass() throws Exception {
    AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
    Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
        UserResponse.newBuilder()
            .setId(validUserId)
            .setUsername("NewTestUser")
            .addAllRoles(List.of(UserRole.TEACHER))
            .build()
    );
    CommentModel comment = new CommentModel(1, 5, "This is a new Test Comment");
    Mockito.when(commentService.getCommentById(any(int.class))).thenReturn(Optional.of(comment));

    Mockito.when(commentService.deleteCommentById(any(int.class))).thenReturn(true);

    mockMvc.perform(delete("/group_feed/delete_comment/1"))
        .andExpect(status().isOk());
  }

  /**
   * This method tests the deletion of the comment using the commentId. Where userId of
   * authenticated User and the userId of comment doesn't match, but the user Is ADMIN.
   *
   * @throws Exception
   */
  @Test
  void deleteACommentUsingCommentIdUserIsAdminExpectPass() throws Exception {
    AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
    Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
        UserResponse.newBuilder()
            .setId(validUserId)
            .setUsername("NewTestUser")
            .addAllRoles(List.of(UserRole.COURSE_ADMINISTRATOR))
            .build()
    );
    CommentModel comment = new CommentModel(1, 5, "This is a new Test Comment");
    Mockito.when(commentService.getCommentById(any(int.class))).thenReturn(Optional.of(comment));

    Mockito.when(commentService.deleteCommentById(any(int.class))).thenReturn(true);

    mockMvc.perform(delete("/group_feed/delete_comment/1"))
        .andExpect(status().isOk());
  }
}
