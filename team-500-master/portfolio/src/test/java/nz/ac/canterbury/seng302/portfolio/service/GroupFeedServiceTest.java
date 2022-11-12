package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.contract.CommentContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.repository.CommentModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ReactionModelRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class GroupFeedServiceTest {

    @InjectMocks
    private PostService postService;

    @InjectMocks
    private ReactionService reactionService;

    @Mock
    private CommentModelRepository commentModelRepository;

    @Mock
    private ReactionModelRepository reactionModelRepository;

    @Mock
    private PostModelRepository mockPostModelRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    SubscriptionService subscriptionService;

    @Mock
    UserAccountService userAccountService;

    @Mock
    GroupsClientService groupsClientService;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentService commentServiceMock;

    @Mock
    private SimpMessagingTemplate template;

    private PostModel newPost;
    private PostModel newPost1;
    private PostModel newPost2;
    private PostModel newPost3;
    private List<PostModel> postList;

    @BeforeEach
    public void setup() {
        mockPostModelRepository.deleteAll();
        newPost = new PostModel(1, 1, "A test Post by User 1");
        newPost1 = new PostModel(1, 2, "A test Post by User 2");
        newPost2 = new PostModel(2, 1, "A test Post by User 1 Again");
        newPost3 = new PostModel(3, 5, "A test Post by User 5");

        postList = new ArrayList<PostModel>();
        postList.add(newPost);
        postList.add(newPost1);
        postList.add(newPost2);
        postList.add(newPost3);

        postList.forEach(model -> {
            mockPostModelRepository.save(model);
        });
    }

    /**
     * This tests that service can retrieve all the posts in the database.
     * @throws Exception
     */
    @Test
    void getAllPostsExpectPass () throws Exception {
        Mockito.when(mockPostModelRepository.findAll()).thenReturn(postList);
        var result = postService.getAllPosts();
        for (int i=0; i<result.size(); i++) {
            Assertions.assertEquals(postList.get(i).getGroupId(), result.get(i).getGroupId());
            Assertions.assertEquals(postList.get(i).getUserId(), result.get(i).getUserId());
            Assertions.assertEquals(postList.get(i).getPostContent(), result.get(i).getPostContent());
        }
    }

    /**
     * This tests that A user should be able to see the posts made by a specific group. it retrieves all the posts made
     * by a group and returns them as a list.
     * @throws Exception
     */
    @Test
    void getAllPostsForGivenGroupId() throws Exception {
        int groupId = 1;
        var newPostList = postList.stream().filter(postModel -> {
                                            return postModel.getGroupId() == groupId;
                                        }).collect(Collectors.toList());
        Mockito.when(mockPostModelRepository.findPostModelByGroupId(groupId))
                .thenReturn(newPostList);

        var result = postService.getAllPostsForAGroup(groupId);
        Assertions.assertTrue(result.size() > 0);
        for (int i=0; i<result.size(); i++) {
            Assertions.assertEquals(newPostList.get(i).getId(), result.get(i).getId());
            Assertions.assertEquals(newPostList.get(i).getGroupId(), result.get(i).getGroupId());
            Assertions.assertEquals(newPostList.get(i).getPostContent(), result.get(i).getPostContent());
        };
    }

    /**
     * If there is no post with the given ID, the function will rerun null.
     * @throws Exception
     */
    @Test
    void getAPostWithPostIdThatDoesNotExistExpectFail () throws Exception {
        int postId = 100;
        Mockito.when(mockPostModelRepository.findById(postId)).thenThrow(new NoSuchElementException());
        var result = postService.getPostById(postId);
        Assertions.assertNull(result);

    }

    /**
     * Delete a post with a given ID, if it passes it will return true and false otherwise.
     * @throws Exception
     */
    @Test
    void deleteAPostWithAValidPostIdExpectPass () throws Exception {
        Mockito.when(mockPostModelRepository.findById(newPost1.getId())).thenReturn(Optional.ofNullable(newPost1));
        Mockito.when(commentService.deleteAllCommentByPostId(newPost1.getId())).thenReturn(true);
        var result = postService.deletePost(newPost1.getId());
        Assertions.assertTrue(result);
    }

    /**
     * Delete a post with the given ID that doesn't exist, method will return false.
     * @throws Exception
     */
    @Test
    void deleteAPostThatDoesNotExistExpectFail () throws Exception {
        Assertions.assertFalse(postService.deletePost(1000));
    }

    /**
     * create a new post with all the valid data for the fields, and it passes the test as expected.
     * @throws Exception
     */
    @Test
    void createANewPostWithValidParamsExpectPass () throws Exception {
        Mockito.when(mockPostModelRepository.save(newPost)).thenReturn(newPost);
        PostContract postContract = new PostContract(newPost.getGroupId(), newPost.getPostContent());

        //build a new GroupDetailsResponse
        GroupDetailsResponse groupDetailsResponse = GroupDetailsResponse.newBuilder()
                .setGroupId(newPost.getGroupId())
                .setShortName("Test")
                .setLongName("Test")
                .build();

        UserResponse userResponse = UserResponse.newBuilder()
                .setUsername("Test")
                .build();

        List<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);
        userIds.add(3);

        Mockito.when(groupsClientService.getGroupById(newPost.getGroupId())).thenReturn(groupDetailsResponse);
        Mockito.when(subscriptionService.getAllByGroupId(anyInt())).thenReturn(List.of(7,8,9));
        Mockito.when(userAccountService.getUserById(newPost.getUserId())).thenReturn(userResponse);




        Boolean result = postService.createPost(postContract, newPost.getUserId());

        Mockito.verify(notificationService, Mockito.times(3)).create(any());
        Assertions.assertTrue(result);
    }

    /**
     * Verify the notification service is called when a new post is created.
     * @throws Exception
     */
    @Test
    void createANewPostWithValidParamsExpectNotificationCall () throws Exception {
        Mockito.when(mockPostModelRepository.save(newPost)).thenReturn(newPost);
        PostContract postContract = new PostContract(newPost.getGroupId(), newPost.getPostContent());

        //build a new GroupDetailsResponse
        GroupDetailsResponse groupDetailsResponse = GroupDetailsResponse.newBuilder()
                .setGroupId(newPost.getGroupId())
                .setShortName("Test")
                .setLongName("Test")
                .build();

        UserResponse userResponse = UserResponse.newBuilder()
                .setUsername("Test")
                .build();

        List<Integer> userIds = new ArrayList<>();
        userIds.add(1);
        userIds.add(2);
        userIds.add(3);

        Mockito.when(groupsClientService.getGroupById(newPost.getGroupId())).thenReturn(groupDetailsResponse);
        Mockito.when(subscriptionService.getAllByGroupId(anyInt())).thenReturn(List.of(7,8,9));
        Mockito.when(userAccountService.getUserById(newPost.getUserId())).thenReturn(userResponse);




        Boolean result = postService.createPost(postContract, newPost.getUserId());

        Mockito.verify(notificationService, Mockito.times(3)).create(any());
    }

    /**
     * create a post where the post content is empty, it should fail because post content is mandatory.
     * @throws Exception
     */
    @Test
    void createANewPostWithInvalidParamsExpectFail () throws Exception {
        PostContract postContract = new PostContract(newPost.getGroupId(), "");
        var result = postService.createPost(postContract, 1);
        Assertions.assertFalse(result);
    }

    /**
     * Update a post content, it's the same user who made the post so he can update it successfully, hence it passes as
     * expected.
     * @throws Exception
     */
    @Test
    void updateAPostExpectPass () throws Exception {
        Mockito.when(mockPostModelRepository.findById(newPost.getId())).thenReturn(Optional.ofNullable(newPost));
        PostContract postUpdate = new PostContract(1, "This is An UPDATED post");
        newPost.setPostContent(postUpdate.postContent());
        Mockito.when(mockPostModelRepository.save(newPost)).thenReturn(newPost);
        Assertions.assertTrue(postService.updatePost(postUpdate, newPost.getId()));
    }

    /**
     * High five a post and notifications should be sent out as expected
     */
    @Test
    void highFivePostAndSendNotifications() {
        //reacted by a different user
        int userId = newPost.getUserId() + 1;
        PostReactionContract postReactionContract = new PostReactionContract(newPost.getId(),userId);
        Mockito.when(mockPostModelRepository.findById(newPost.getId())).thenReturn(Optional.ofNullable(newPost));
        Mockito.when(userAccountService.getUserById(userId)).thenReturn(UserResponse.newBuilder().setId(userId).build());

        reactionService.addHighFiveToPost(postReactionContract);

        Mockito.verify(notificationService, Mockito.times(1)).create(any());
    }

    /**
     * Comment on a post and notifications should be sent out as expected
     */
    @Test
    void commentOnPostAndSendNotifications() {
        //Comment by a different user
        int userId = newPost.getUserId() + 1;
        CommentContract commentContract = new CommentContract(userId, newPost.getId(), "test");
        Mockito.when(mockPostModelRepository.findById(newPost.getId())).thenReturn(Optional.ofNullable(newPost));
        Mockito.when(userAccountService.getUserById(userId)).thenReturn(UserResponse.newBuilder().setId(userId).build());

        commentServiceMock.addNewCommentsToPost(commentContract);

        Mockito.verify(notificationService, Mockito.times(1)).create(any());
    }

    @Test
    void checkTimeStampOnUpdatedPosts () throws Exception {
        PostContract testPost1 = new PostContract(1, "this is a post");
        PostModel post1Model = new PostModel(testPost1.groupId(), 3, testPost1.postContent());

        Assertions.assertNull(post1Model.getUpdated());
        Assertions.assertFalse(post1Model.isPostUpdated());

        var timeBeforeChange = post1Model.getCreated();
        post1Model.setPostContent("This is an updated post...");
        var timeAfterChange = post1Model.getCreated();

        Assertions.assertNotNull(timeBeforeChange);
        Assertions.assertNotNull(timeAfterChange);
        Assertions.assertEquals(timeBeforeChange, timeAfterChange);

        Assertions.assertNotNull(post1Model.getUpdated());
        Assertions.assertTrue(post1Model.isPostUpdated());
    }
}
