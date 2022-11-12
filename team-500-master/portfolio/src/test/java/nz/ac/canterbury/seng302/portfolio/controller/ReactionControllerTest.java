package nz.ac.canterbury.seng302.portfolio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostReactionContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.ReactionService;
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
class ReactionControllerTest {

  List<String> usernames = new ArrayList<>();
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ReactionController reactionController;
  @MockBean
  private ReactionService reactionService;
  @MockBean
  private AuthStateService authStateService;
  @MockBean
  private UserAccountService userAccountService;
  private int validUserId = 3;
  private int validPostId = 1;
  private int validCommentId = 1;

  @BeforeEach
  void setup() {
    Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
        UserResponse.newBuilder()
            .setId(validUserId)
            .setUsername("testing")
            .build()
    );

    Mockito.when(authStateService.getId(any(PortfolioPrincipal.class))).thenReturn(validUserId);
    AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);
    usernames.add("user1");
    usernames.add("user2");
    usernames.add("user3");
  }

  @Test
  void contextLoads() throws Exception {
    Assertions.assertNotNull(reactionController);
  }

  @Test
  void getAllUserNameForUsersWhoReactedToAPostExpectPass() throws Exception {

    Mockito.when(reactionService.getUsernamesOfUsersWhoReactedToPost(validPostId))
        .thenReturn(usernames);

    mockMvc.perform(get("/group_feed/reaction_post/" + validPostId))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"usernames\":[\"user1\",\"user2\", \"user3\"]}"))
        .andReturn();
  }

  @Test
  void getAllUserNameForUsersWhoReactedToAPostExpectFail() throws Exception {
    Mockito.when(reactionService.getUsernamesOfUsersWhoReactedToPost(validPostId))
        .thenReturn(new ArrayList<>());
    mockMvc.perform(get("/reaction_post/" + validPostId))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void getAllUserNameForUsersWhoReactedToACommentExpectPass() throws Exception {
    Mockito.when(reactionService.getUsernamesOfUsersWhoReactedToComment(any(int.class)))
        .thenReturn(usernames);

    mockMvc.perform(get("/group_feed/reaction_comment/" + validCommentId))
        .andExpect(status().isOk())
        .andExpect(content().json("{\"usernames\":[\"user1\",\"user2\", \"user3\"]}"))
        .andReturn();
  }

  @Test
  void getAllUserNameForUsersWhoReactedToACommentExpectFail() throws Exception {
    Mockito.when(reactionService.getUsernamesOfUsersWhoReactedToComment(any(int.class)))
        .thenReturn(new ArrayList<>());
    mockMvc.perform(get("/group_feed/reaction_comment/" + validCommentId))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void postANewReactionToAPostAndExpectToPass() throws Exception {
    Mockito.when(reactionService.processPostHighFive(any(PostReactionContract.class)))
        .thenReturn(true);
    mockMvc.perform(post("/group_feed/post_high_five")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "postId" : "1"
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

  @Test
  void postANewReactionToACommentAndExpectToPass() throws Exception {
    Mockito.when(reactionService.processPostHighFive(any(PostReactionContract.class)))
        .thenReturn(true);
    mockMvc.perform(post("/group_feed/comment_high_five")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "postId" : "1"
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
  }

}
