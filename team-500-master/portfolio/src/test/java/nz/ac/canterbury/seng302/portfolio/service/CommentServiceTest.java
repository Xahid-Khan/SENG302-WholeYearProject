package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.contract.CommentContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.CommentModel;
import nz.ac.canterbury.seng302.portfolio.repository.CommentModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.SubscriptionRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentModelRepository mockCommentRepository;

    @Mock
    private PostModelRepository postModelRepository;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private NotificationService notificationService;

    private CommentContract comment1;
    private CommentContract comment2;
    private CommentContract comment3;
    private CommentContract comment4;

    private List<CommentModel> commentList;

    @BeforeEach
    void setup () {
        mockCommentRepository.deleteAll();
        comment1 = new CommentContract(1, 1, "This is a cool post");
        comment2 = new CommentContract(4, 3, "This is a comment to a comment");
        comment3 = new CommentContract(2, 2, "This is new Comment to a post");
        comment4 = new CommentContract(100, 10000, "A Comment to 10000th post of the group");
        commentList = new ArrayList<>();
        commentList.add(new CommentModel(comment1.postId(), comment1.userId(), comment1.comment()));
        commentList.add(new CommentModel(comment2.postId(), comment1.userId(), comment1.comment()));
        commentList.add(new CommentModel(comment3.postId(), comment1.userId(), comment1.comment()));
        commentList.add(new CommentModel(comment4.postId(), comment1.userId(), comment1.comment()));

        commentList.forEach(comment -> {
            mockCommentRepository.save(comment);
        });
    }

    /**
     * This test gets all the comments from the database.
     * @throws Exception
     */
    @Test
    void getAllCommentsExpectPass () throws Exception {
        Mockito.when(mockCommentRepository.findAll()).thenReturn(commentList);
        var result = commentService.getAllComments();
        Assertions.assertNotNull(result);
        for (int i=0; i<result.size(); i++) {
            Assertions.assertEquals(commentList.get(i).getPostId(), result.get(i).getPostId());
            Assertions.assertEquals(commentList.get(i).getUserId(), result.get(i).getUserId());
            Assertions.assertEquals(commentList.get(i).getCommentContent(), result.get(i).getCommentContent());
        }
    }

    /**
     * This test gets all the comment for a given post.
     * @throws Exception
     */
    @Test
    void getAllCommentsByPostIdExpectPass () throws Exception  {
        var postId = 1;
        ArrayList<CommentModel> filteredCommentsByPost = (ArrayList<CommentModel>) commentList.stream().filter(
                comment -> {
                   return comment.getPostId() == postId;}).collect(Collectors.toList());
        Mockito.when(mockCommentRepository.findAllCommentByPostId(postId)).thenReturn(filteredCommentsByPost);

        var result = commentService.getCommentsForGivenPost(postId);
        for (int i=0; i<filteredCommentsByPost.size(); i++) {
            Assertions.assertEquals(filteredCommentsByPost.get(i).getPostId(), result.get(i).getPostId());
            Assertions.assertEquals(filteredCommentsByPost.get(i).getUserId(), result.get(i).getUserId());
            Assertions.assertEquals(filteredCommentsByPost.get(i).getCommentContent(), result.get(i).getCommentContent());
        }
    }

    /**
     * This test adds a new comment to a post, with all the valid fields hence it passes.
     * @throws Exception
     */
    @Test
    void AddNewCommentToAPostExpectPass () throws Exception  {
        Mockito.when(notificationService.create(any())).thenReturn(null);
        Mockito.when(mockCommentRepository.save(commentList.get(0))).thenReturn(null);
        Mockito.when(userAccountService.getUserById(anyInt())).thenReturn(UserResponse.newBuilder().build());
        var result = commentService.addNewCommentsToPost(comment1);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentList.get(0).getPostId(), result.getPostId());
        Assertions.assertEquals(commentList.get(0).getUserId(), result.getUserId());
        Assertions.assertEquals(commentList.get(0).getCommentContent(), result.getCommentContent());
    }

    /**
     * This test, updates a comment made by a user
     * @throws Exception
     */
    @Test
    void UpdateACommentExpectPass () throws Exception  {
        Mockito.when(mockCommentRepository.findById(commentList.get(0).getId())).thenReturn(Optional.ofNullable(commentList.get(0)));
        CommentModel updatedComment = commentList.get(0);
        updatedComment.setCommentContent("This comment has been edited");
        Mockito.when(mockCommentRepository.save(updatedComment)).thenReturn(updatedComment);

        CommentContract newContract = new CommentContract(comment1.userId(), comment1.postId(), "This comment has been edited");

        var result = commentService.updateComment(commentList.get(0).getId(), newContract);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(updatedComment.getCommentContent(), result.getCommentContent());
    }

    /**
     * This test deletes a comment made by a user
     * @throws Exception
     */
    @Test
    void deleteCommentsByCommentIdExpectPass () throws Exception {
        List<CommentModel> filteredList = commentList.stream().filter(comment -> {
            return comment.getId() != commentList.get(0).getId();
        }).collect(Collectors.toList());

        Mockito.when(mockCommentRepository.deleteById(commentList.get(0).getId())).thenReturn(true);
        Mockito.when(mockCommentRepository.findAll()).thenReturn(filteredList);

        var deletedComment = commentService.deleteCommentById(commentList.get(0).getId());
        Assertions.assertTrue(deletedComment);

        var result = commentService.getAllComments();
        for (int i=0; i < filteredList.size(); i++) {
            Assertions.assertEquals(filteredList.get(i).getUserId(), result.get(i).getUserId());
            Assertions.assertEquals(filteredList.get(i).getPostId(), result.get(i).getPostId());
            Assertions.assertEquals(filteredList.get(i).getCommentContent(), result.get(i).getCommentContent());
        }
    }

    /**
     * This test tries to delete the comment made by different user and it fails.
     * @throws Exception
     */
    @Test
    void deleteCommentsByInvalidCommentIdExpectFail () throws Exception {
        int commentId = 1000000;
        List<CommentModel> filteredList = commentList.stream().filter(comment -> {
            return comment.getId() != commentId;
        }).collect(Collectors.toList());

        Mockito.when(mockCommentRepository.deleteById(commentId)).thenReturn(false);
        Mockito.when(mockCommentRepository.findAll()).thenReturn(filteredList);

        var deletedComment = commentService.deleteCommentById(commentList.get(0).getId());
        Assertions.assertFalse(deletedComment);

        var result = commentService.getAllComments();
        for (int i=0; i < filteredList.size(); i++) {
            Assertions.assertEquals(filteredList.get(i).getUserId(), result.get(i).getUserId());
            Assertions.assertEquals(filteredList.get(i).getPostId(), result.get(i).getPostId());
            Assertions.assertEquals(filteredList.get(i).getCommentContent(), result.get(i).getCommentContent());
        }
    }

    /**
     * This method deletes all the comments for a post, it will only happen once a post is deleted.
     * @throws Exception
     */
    @Test
    void deleteAllCommentsByPostIdExpectPass () throws Exception {
        Mockito.when(mockCommentRepository.deleteCommentsByPostId(commentList.get(0).getPostId())).thenReturn(true);
        Mockito.when(mockCommentRepository.findAllCommentByPostId(commentList.get(0).getPostId())).thenReturn(new ArrayList<>());

        var deleteComments = commentService.deleteAllCommentByPostId(commentList.get(0).getPostId());
        Assertions.assertTrue(deleteComments);

        var result = commentService.getCommentsForGivenPost(commentList.get(0).getPostId());
        Assertions.assertEquals(new ArrayList<>(), result);
    }

    /**
     * this tests fails as the post ID is not valid, hence you cannot delete a post or comments that doesn't exist.
     * @throws Exception
     */
    @Test
    void deleteAllCommentsByInvalidPostIdExpectFail () throws Exception {
        int postId = 10000;
        Mockito.when(mockCommentRepository.deleteCommentsByPostId(postId)).thenReturn(false);
        Mockito.when(mockCommentRepository.findAllCommentByPostId(postId)).thenReturn(new ArrayList<>());

        var deleteComments = commentService.deleteAllCommentByPostId(postId);
        Assertions.assertFalse(deleteComments);

        var result = commentService.getCommentsForGivenPost(postId);
        Assertions.assertEquals(new ArrayList<>(), result);
    }
}
