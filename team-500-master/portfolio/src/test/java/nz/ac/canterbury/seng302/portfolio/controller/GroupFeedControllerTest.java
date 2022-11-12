package nz.ac.canterbury.seng302.portfolio.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.AuthorisationParamsHelper;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.controller.feed.GroupFeedController;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.CommentModel;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.CommentService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.PostService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
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
class GroupFeedControllerTest {
    private final int validUserId = 3;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GroupFeedController controller;
    @MockBean
    private PostService postService;
    @MockBean
    private CommentService commentService;
    @MockBean
    private UserAccountService userAccountService;
    @MockBean
    private GroupsClientService groupsClientService;
    @MockBean
    private AuthStateService authStateService;
    private PostModel post1;
    private PostModel post2;
    private PostModel post3;
    private PostModel post4;

    private CommentModel comment1;
    private CommentModel comment2;
    private CommentModel comment3;
    private CommentModel comment4;

    private List<PostModel> allPosts;
    private List<CommentModel> allComments;

    @BeforeEach
    void setupBeforeEach () {
        Mockito.when(groupsClientService.getGroupById(any(int.class))).thenReturn(GroupDetailsResponse.newBuilder()
                .setGroupId(1)
                .setShortName("Mock Group")
                .setLongName("This is a new test Group").build());

        Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
                UserResponse.newBuilder()
                        .setId(validUserId)
                        .setUsername("testing")
                        .addRoles(UserRole.STUDENT)
                        .build()
        );
        Mockito.when(authStateService.getId(any(PortfolioPrincipal.class))).thenReturn(3);
        AuthorisationParamsHelper.setParams("role", UserRole.STUDENT);

        post1 = new PostModel(1, validUserId, "This a new Post From Teachers");
        post2 = new PostModel(2, validUserId, "This a Test Post From Other Group");
        post3 = new PostModel(3, 4, "This a Test Post From Group 3");
        post4 = new PostModel(1, 2, "This another post from teachers.");

        allPosts = new ArrayList<>();
        allPosts.add(post1);
        allPosts.add(post2);
        allPosts.add(post3);
        allPosts.add(post4);

        comment1 = new CommentModel(post1.getId(), 4, "This a reply to teachers' post");
        comment2 = new CommentModel(post2.getId(), validUserId, "This message to group 2 post");
        comment3 = new CommentModel(post1.getId(), 4, "This a reply to the post from teachers");
        comment4 = new CommentModel(post3.getId(), validUserId, "This a reply to group 3");

        allComments = new ArrayList<>();
        allComments.add(comment1);
        allComments.add(comment2);
        allComments.add(comment3);
        allComments.add(comment4);
    }

    /**
     * This test makes sure that controller is loaded and running.
     * @throws Exception
     */
    @Test
    void contextLoads() throws Exception {
        Assertions.assertNotNull(controller);
    }

    /**
     * This test creates a new post will all the valid values and expects a success code
     * @throws Exception
     */
    @Test
    void createNewPostWithValidFieldsAndExpectPass () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(true);
        Mockito.when(postService.createPost(any(PostContract.class), any(int.class))).thenReturn(true);

        mockMvc.perform(post("/group_feed/new_post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "groupId" : "1",
                                    "postContent" : "This a test dummy post"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    /**
     * This test checks if the User how is not member of the group can post on that group feed page or not, and it fails
     * as expected.
     * @throws Exception
     */
    @Test
    void createNewPostUserNotMemberOfGroupExpectFail () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(false);
        Mockito.when(postService.createPost(new PostContract(1,"This a test dummy post"), 1)).thenReturn(true);
        mockMvc.perform(post("/group_feed/new_post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "groupId" : "1",
                                    "postContent" : "This a test dummy post"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    /**
     * This tests deletion of a post, the user who made the post is deleting the post, so it is expected to pass.
     * @throws Exception
     */
    @Test
    void deleteAPostWhereUserIsWhoMadeThePostExpectPass () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(true);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post1);

        mockMvc.perform(delete("/group_feed/delete_feed/1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(status().isOk());
    }

    /**
     * this tests deleteion of a post made by a different user, and it fails as expected as a user cannot delete posts made
     * by other users.
     * @throws Exception
     */
    @Test
    void deleteAPostWhereUserIsNotWhoMadeThePostExpectFail () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(true);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post3);
        mockMvc.perform(delete("/delete_feed/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    /**
     * A user cannot delete a post, once he/she is no longer part of that group.
     * @throws Exception
     */
    @Test
    void deleteAPostWhereUserIsWhoMadeThePostButNotMemberAnyMoreExpectFail () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(false);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post1);
        mockMvc.perform(delete("/delete_feed/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }


    /**
     * A Teacher can delete any post made by any member in any group.
     * @throws Exception
     */
    @Test
    void deleteAPostWhereUserIsATeacherExpectPass () throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.TEACHER);
      Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(validUserId)
              .setUsername("testing")
              .addRoles(UserRole.TEACHER)
              .build()
      );
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(false);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post3);
        mockMvc.perform(delete("/group_feed/delete_feed/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * An Admin can delete any post made by any member in any group.
     * @throws Exception
     */
    @Test
    void deleteAPostWhereUserIsAAdminExpectPass () throws Exception {
        AuthorisationParamsHelper.setParams("role", UserRole.COURSE_ADMINISTRATOR);
      Mockito.when(userAccountService.getUserById(any(int.class))).thenReturn(
          UserResponse.newBuilder()
              .setId(validUserId)
              .setUsername("testing")
              .addRoles(UserRole.COURSE_ADMINISTRATOR)
              .build()
      );
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(false);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post3);
        mockMvc.perform(delete("/group_feed/delete_feed/3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * A user who made the post can update the content of the post any time.
     * @throws Exception
     */
    @Test
    void updateAPostWhereUserIsWhoMadeOriginalPostExpectPass () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(true);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post1);
        Mockito.when(postService.updatePost(any(PostContract.class), any(int.class))).thenReturn(true);

        mockMvc.perform(put("/group_feed/update_feed/" + post1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "groupId" : "1",
                                    "postContent" : "This a test dummy post Changed From Teachers"
                                }
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    /**
     * A post cannot be updated by any user other than the user who made the post, Event Teachers/Admins cannot edit a post.
     * @throws Exception
     */
    @Test
    void updateAPostWhereUserIsNotWhoMadeOriginalPostExpectFail () throws Exception {
        Mockito.when(groupsClientService.isMemberOfTheGroup(any(int.class), any(int.class))).thenReturn(true);
        Mockito.when(postService.getPostById(any(int.class))).thenReturn(post4);
        Mockito.when(postService.updatePost(any(PostContract.class), any(int.class))).thenReturn(true);

    mockMvc.perform(put("/update_feed/" + post4.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "groupId" : "1",
                    "postContent" : "This a test dummy post Changed From Teachers"
                }
                """)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError())
        .andReturn();
  }
}
