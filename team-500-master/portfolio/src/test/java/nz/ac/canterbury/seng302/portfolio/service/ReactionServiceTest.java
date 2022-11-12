package nz.ac.canterbury.seng302.portfolio.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.portfolio.model.contract.CommentReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.ReactionModel;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ReactionModelRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ReactionServiceTest {

  @InjectMocks
  private ReactionService reactionService;
  @Mock
  private ReactionModelRepository mockReactionRepository;
  @Mock
  private UserAccountService userAccountService;
  @Mock
  private PostModelRepository postModelRepository;
  @Mock
  private NotificationService notificationService;

  private CommentReactionContract commentReactionContract1;
  private CommentReactionContract commentReactionContract2;
  private CommentReactionContract commentReactionContract3;

  private PostReactionContract postReactionContract1;
  private PostReactionContract postReactionContract2;
  private PostReactionContract postReactionContract3;

  private List<ReactionModel> allReactionsPosts;
  private List<ReactionModel> allReactionsComments;
  private UserResponse user;
  private UserResponse user1;
  private UserResponse user2;
  private UserResponse user3;

  @BeforeEach
  void setup() {
    mockReactionRepository.deleteAll();
    allReactionsPosts = new ArrayList<>();
    allReactionsComments = new ArrayList<>();
    commentReactionContract1 = new CommentReactionContract(1, 1, 1);
    commentReactionContract2 = new CommentReactionContract(1, 2, 2);
    commentReactionContract3 = new CommentReactionContract(2, 3, 1);
    ReactionModel comment1 = new ReactionModel(commentReactionContract1.userId(),
        commentReactionContract1.postId(), commentReactionContract1.commentId());
    ReactionModel comment2 = new ReactionModel(commentReactionContract2.userId(),
        commentReactionContract2.postId(), commentReactionContract2.commentId());
    ReactionModel comment3 = new ReactionModel(commentReactionContract3.userId(),
        commentReactionContract3.postId(), commentReactionContract3.commentId());
    allReactionsComments.add(comment1);
    allReactionsComments.add(comment2);
    allReactionsComments.add(comment3);

    postReactionContract1 = new PostReactionContract(1, 1);
    postReactionContract2 = new PostReactionContract(2, 1);
    postReactionContract3 = new PostReactionContract(1, 3);
    ReactionModel comment4 = new ReactionModel(postReactionContract1.userId(),
        postReactionContract1.postId());
    ReactionModel comment5 = new ReactionModel(postReactionContract2.userId(),
        postReactionContract2.postId());
    ReactionModel comment6 = new ReactionModel(postReactionContract3.userId(),
        postReactionContract3.postId());
    allReactionsPosts.add(comment4);
    allReactionsPosts.add(comment5);
    allReactionsPosts.add(comment6);

    user = UserResponse.newBuilder()
        .setId(3)
        .setUsername("NewUser")
        .build();
    user1 = UserResponse.newBuilder()
        .setId(3)
        .setUsername("NewUser1")
        .build();
    user2 = UserResponse.newBuilder()
        .setId(4)
        .setUsername("NewUser2")
        .build();
    user3 = UserResponse.newBuilder()
        .setId(5)
        .setUsername("NewUser3")
        .build();
  }

  /**
   * Get all the reactions from the database for a user, either he has reacted to a post or a
   * comment. using the user id.
   */
  @Test
  void getAllReactionsByUserIdAndExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByUserId(1))
        .thenReturn(
            allReactionsComments.stream().filter(reaction -> reaction.getUserId() == 1).collect(
                Collectors.toList()));
    var result = reactionService.getReactionsByUserId(1);
    Assertions.assertTrue(result.size() > 0);
    result.forEach(reaction -> {
      Assertions.assertEquals(1, reaction.getUserId());
    });
  }

  /**
   * Get all the reactions from the database for a post, using the post id.
   */
  @Test
  void getAllReactionsByPostIdAndExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByPostId(1))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getPostId() == 1).collect(
                Collectors.toList()));
    var result = reactionService.getReactionByPostId(1);
    Assertions.assertTrue(result.size() > 0);
    result.forEach(reaction -> {
      Assertions.assertEquals(1, reaction.getPostId());
    });
  }

  /**
   * Get all the reactions from the database for a comment, using the comment id.
   */
  @Test
  void getAllReactionsByCommentIdAndExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByCommentId(1))
        .thenReturn(
            allReactionsComments.stream().filter(reaction -> reaction.getCommentId() == 1).collect(
                Collectors.toList()));
    var result = reactionService.getReactionByCommentId(1);
    Assertions.assertTrue(result.size() > 0);
    result.forEach(reaction -> {
      Assertions.assertEquals(1, reaction.getCommentId());
    });
  }

  /**
   * Get all the reactions from the database for a user, either he has reacted to a post or a
   * comment. using the user id. But the returned list will be empty because this user has not
   * reacted to any post or comments.
   */
  @Test
  void getAllReactionsByUserIdAndExpectFail() {
    Mockito.when(mockReactionRepository.getReactionsByUserId(-500))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getUserId() == -500).collect(
                Collectors.toList()));
    var result = reactionService.getReactionsByUserId(-500);
    Assertions.assertFalse(result.size() > 0);
  }

  /**
   * Get all the reactions from the database for a post, using the post id. Expect fail because the
   * post has no reaction.
   */
  @Test
  void getAllReactionsByPostIdAndExpectFail() {
    Mockito.when(mockReactionRepository.getReactionsByPostId(-500))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getPostId() == -500).collect(
                Collectors.toList()));
    var result = reactionService.getReactionByPostId(-500);
    Assertions.assertFalse(result.size() > 0);
  }

  /**
   * Get all the reactions from the database for a comment, using the post id. Expect fail because
   * the comment has no reaction.
   */
  @Test
  void getAllReactionsByCommentIdAndExpectFail() {
    Mockito.when(mockReactionRepository.getReactionsByCommentId(-500))
        .thenReturn(
            allReactionsComments.stream().filter(reaction -> reaction.getCommentId() == -500)
                .collect(
                    Collectors.toList()));
    var result = reactionService.getReactionByCommentId(-500);
    Assertions.assertFalse(result.size() > 0);
  }

  /**
   * This function will test that the user can react to a post if he/she has not already reacted to
   * that post.
   */
  @Test
  void processAPostHighFiveThatUserHasNotReactedToExpectPass() {
    Mockito.when(notificationService.create(any())).thenReturn(null);
    Mockito.when(mockReactionRepository.getReactionsByUserId(any(int.class)))
        .thenReturn(new ArrayList<>());
    Mockito.when(mockReactionRepository.save(any())).thenReturn(allReactionsPosts.get(1));
    Mockito.when(userAccountService.getUserById(anyInt())).thenReturn(UserResponse.newBuilder().build());

    var result = reactionService.processPostHighFive(postReactionContract1);
    Assertions.assertTrue(result);
    verify(mockReactionRepository).save(any());
  }

  /**
   * This function will test that if the user has already reacted to the post and press the
   * high-five reaction tab again it will remove the reaction from the post.
   */
  @Test
  void processAPostHighFiveIfUserAlreadyReactedThanReactingAgainWillRemoveTheReaction() {
    Mockito.when(mockReactionRepository.getReactionsByUserId(1))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getUserId() == 1).collect(
                Collectors.toList()));
    Mockito.doNothing().when(mockReactionRepository).deleteById(any());

    var result = reactionService.processPostHighFive(postReactionContract1);
    Assertions.assertTrue(result);
    verify(mockReactionRepository).deleteById(any());
  }

  /**
   * This function will test that the user can react to a comment if he/she has not already reacted
   * to that comment.
   */
  @Test
  void processACommentHighFiveThatUserHasNotReactedToExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByUserId(any(int.class)))
        .thenReturn(new ArrayList<>());
    Mockito.when(mockReactionRepository.save(any())).thenReturn(allReactionsComments.get(0));

    var result = reactionService.processCommentHighFive(commentReactionContract1);

    Assertions.assertTrue(result);
    verify(mockReactionRepository).save(any());
  }

  /**
   * This function will test that if the user has already reacted to the comment and press the
   * high-five reaction tab again it will remove the reaction from the comment.
   */
  @Test
  void processACommentHighFiveIfUserAlreadyReactedThanReactingAgainWillRemoveTheReaction() {
    Mockito.when(mockReactionRepository.getReactionsByUserId(1))
        .thenReturn(
            allReactionsComments.stream().filter(reaction -> reaction.getUserId() == 1).collect(
                Collectors.toList()));
    Mockito.doNothing().when(mockReactionRepository).deleteById(any());

    var result = reactionService.processCommentHighFive(commentReactionContract1);
    Assertions.assertTrue(result);
    verify(mockReactionRepository).deleteById(any());
  }

  /**
   * This function will test that a list of usernames of users who reacted to a post is returned.
   */
  @Test
  void getAListOfUsersNamesForPostExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByPostId(any(int.class)))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getUserId() == 1).collect(
                Collectors.toList()));

    Mockito.when(userAccountService.getUserById(any(int.class)))
        .thenReturn(user)
        .thenReturn(user1)
        .thenReturn(user2)
        .thenReturn(user3);

    var result = reactionService.getUsernamesOfUsersWhoReactedToPost(1);

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.size() > 0);
  }


  /**
   * This function will test that a list of usernames of users who reacted to a comment is returned.
   */
  @Test
  void getAListOfUsersNamesForCommentExpectPass() {
    Mockito.when(mockReactionRepository.getReactionsByCommentId(any(int.class)))
        .thenReturn(
            allReactionsPosts.stream().filter(reaction -> reaction.getUserId() == 1).collect(
                Collectors.toList()));

    Mockito.when(userAccountService.getUserById(any(int.class)))
        .thenReturn(user)
        .thenReturn(user1)
        .thenReturn(user2)
        .thenReturn(user3);

    var result = reactionService.getUsernamesOfUsersWhoReactedToComment(1);

    Assertions.assertNotNull(result);
    result.forEach(value -> {
      System.err.println(value);
    });
    Assertions.assertTrue(result.size() > 0);
  }
}
