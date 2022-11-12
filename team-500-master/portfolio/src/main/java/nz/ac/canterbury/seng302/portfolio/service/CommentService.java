package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.model.contract.CommentContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.CommentModel;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.repository.CommentModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is a service to the Comments End-Point to help with CRUD.
 */
@Service
public class CommentService {

  @Autowired
  private CommentModelRepository commentRepository;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  private ReactionService reactionService;

  @Autowired
  private PostModelRepository postModelRepository;

  @Autowired
  private NotificationService notificationService;

  /**
   * This funciton will gather all the comments in the database and return it.
   *
   * @return A List of Comment Models
   */
  public List<CommentModel> getAllComments() {
    try {
      return (ArrayList<CommentModel>) commentRepository.findAll();
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  /**
   * This function will gather all the comments related to a specific post and return them in a
   * list.
   *
   * @param postId Integer Post ID
   * @return A List of Comment Models
   */
  public List<CommentModel> getCommentsForGivenPost(int postId) {
    try {
      List<CommentModel> result = commentRepository.findAllCommentByPostId(postId);
      if (!result.isEmpty()) {
        return result;
      } else {
        return new ArrayList<>();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  /**
   * This function will add a new comment made to a post into the database.
   *
   * @param newComment CommentContract containing postId, UserId and Comment content.
   * @return CommentModel
   */
  public CommentModel addNewCommentsToPost(CommentContract newComment) {
    try {
      CommentModel comment = new CommentModel(newComment.postId(), newComment.userId(),
          newComment.comment());
      commentRepository.save(comment);
      Optional<PostModel> post = postModelRepository.findById(newComment.postId());
      UserResponse user = userAccountService.getUserById(newComment.userId());
      if (post.isPresent()){
      int posterId = post.get().getUserId();
      int commenterId = newComment.userId();
        if( posterId != commenterId) {
          notificationService.create(new BaseNotificationContract(posterId, "Your Posts", user.getUsername() + " commented on your post!"));
        }
      }
      return comment;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function will update the edited comment and save it into the database.
   *
   * @param commentId      Integer Comment Id
   * @param updatedComment CommentContract containing postId, UserId, and Comment content
   * @return updated CommentModel
   */
  public CommentModel updateComment(int commentId, CommentContract updatedComment) {
    try {
      Optional<CommentModel> getComment = commentRepository.findById(commentId);
      if (getComment.isPresent()) {
        CommentModel comment = getComment.get();
        comment.setCommentContent(updatedComment.comment());
        commentRepository.save(comment);
        return comment;
      } else {
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This funciton will delete the comment by the give comment ID.
   *
   * @param commentId Integer comment ID
   * @return True if deletion is successful False otherwise
   */
  public boolean deleteCommentById(int commentId) {
    try {
      return commentRepository.deleteById(commentId);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This function will delete all the comments for a given post.
   *
   * @param postId Integer post ID
   * @return True if deletion is successful False otherwise
   */
  public boolean deleteAllCommentByPostId(int postId) {
    try {
      return commentRepository.deleteCommentsByPostId(postId);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This method returns an optional comment entity if found in the database.
   *
   * @param commentId An integer.
   * @return An optional CommentModel.
   */
  public Optional<CommentModel> getCommentById(int commentId) {
    return commentRepository.findById(commentId);
  }

  /**
   * A helper function that will retrieve all the comments for a given post and return them as a
   * list of Hash Map.
   *
   * @param postId A post ID of type Integer.
   * @return A list containing all the comments for the post as HashMap objects.
   */
  public List<Map<String, Object>> getCommentsForThePostAsJson(int postId) {
    List<Map<String, Object>> comments = new ArrayList<>();
    this.getCommentsForGivenPost(postId).forEach(comment -> {
      Map<String, Object> commentObject = new HashMap<>();
      commentObject.put("commentId", comment.getId());
      commentObject.put("userId", comment.getUserId());
      commentObject.put("username", userAccountService.getUserById(comment.getUserId()).getUsername());
      commentObject.put("time", comment.getCreated());
      commentObject.put("content", comment.getCommentContent());
      commentObject.put("reactions",
          reactionService.getUsernamesOfUsersWhoReactedToComment(comment.getId()));
      comments.add(commentObject);
    });

    return comments;
  }
}
