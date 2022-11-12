package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.ac.canterbury.seng302.portfolio.model.contract.CommentReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.model.entity.ReactionModel;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.portfolio.repository.ReactionModelRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service to the reaction controller, to help add or delete the reactions to a post or comment.
 */
@Service
public class ReactionService {

  @Autowired
  private ReactionModelRepository reactionRepository;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  private PostModelRepository postModelRepository;

  @Autowired
  private NotificationService notificationService;

  /**
   * get all the reactions for a specific user, using the user id.
   *
   * @param userId integer
   * @return List of ReactionModel
   */
  public List<ReactionModel> getReactionsByUserId(int userId) {
    return reactionRepository.getReactionsByUserId(userId);
  }

  /**
   * get all the reaction for a post, using the post id.
   *
   * @param postId integer
   * @return List of ReactionModel
   */
  public List<ReactionModel> getReactionByPostId(int postId) {
    return reactionRepository.getReactionsByPostId(postId);
  }

  /**
   * get all the reactions for a comment, using the comment id.
   *
   * @param commentId integer
   * @return List of ReactionModel
   */
  public List<ReactionModel> getReactionByCommentId(int commentId) {
    return reactionRepository.getReactionsByCommentId(commentId);
  }

  /**
   * This method will check if the user has already reacted to the post or not, if he/she has not
   * reacted to the post then it will call the method to add the reaction to the post else it will
   * call the method to remove the reaction from the post.
   *
   * @param postReactionContract A contract to fulfill the requirements for reacting to a post
   * @return True if successful else False.
   */
  public boolean processPostHighFive(PostReactionContract postReactionContract) {
    List<ReactionModel> reactions = reactionRepository.getReactionsByUserId(
        postReactionContract.userId());

    for (ReactionModel reaction : reactions) {
      if (reaction.getPostId() == postReactionContract.postId()
          && reaction.getUserId() == postReactionContract.userId()) {
        return removeHighFive(reaction.getId());
      }
    }
    return addHighFiveToPost(postReactionContract);
  }

  /**
   * This method will check if the user has already reacted to the comment or not, if he/she has not
   * reacted to the comment then it will call the method to add the reaction to that comment else it
   * will call the method to remove the reaction from that comment.
   *
   * @param commentReactionContract A contract to fulfill the requirements for reacting to a
   *                                comment
   * @return True if successful else False.
   */
  public boolean processCommentHighFive(CommentReactionContract commentReactionContract) {
    List<ReactionModel> reactions = reactionRepository.getReactionsByUserId(
        commentReactionContract.userId());

    for (ReactionModel reaction : reactions) {
      if (reaction.getPostId() == commentReactionContract.postId()
          && reaction.getUserId() == commentReactionContract.userId()
          && reaction.getCommentId() == commentReactionContract.commentId()) {
        return removeHighFive(reaction.getId());
      }
    }
    return addHighFiveToComment(commentReactionContract);
  }

  /**
   * This method will create a new instance of high-five for a post and save it to the database.
   *
   * @param postReactionContract A contract to fulfill the requirements for reacting to a post
   * @return True if successful else False.
   */
  public boolean addHighFiveToPost(PostReactionContract postReactionContract) {
    try {
      ReactionModel newReaction = new ReactionModel(postReactionContract.userId(),
          postReactionContract.postId());
      reactionRepository.save(newReaction);
      Optional<PostModel> post = postModelRepository.findById(postReactionContract.postId());
      UserResponse user = userAccountService.getUserById(postReactionContract.userId());
      if (post.isPresent()){
        int posterId = post.get().getUserId();
        int highFiverId = postReactionContract.userId();
        if( posterId != highFiverId) {
          notificationService.create(new BaseNotificationContract(posterId, "Your Posts", user.getUsername() + " high-fived your post!"));
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * This method will create a new instance of high-five for a comment and save it to the database.
   *
   * @param commentReactionContract A contract to fulfill the requirements for reacting to a
   *                                comment
   * @return True if successful else False.
   */
  public boolean addHighFiveToComment(CommentReactionContract commentReactionContract) {
    try {
      ReactionModel newReaction = new ReactionModel(commentReactionContract.userId(),
          commentReactionContract.postId(), commentReactionContract.commentId());
      reactionRepository.save(newReaction);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * This method will delete/remove an instance of the reaction from the database.
   *
   * @param reactionId id of the reaction (high-five) of type int
   * @return True if deletion is successful else False
   */
  public boolean removeHighFive(int reactionId) {
    try {
      reactionRepository.deleteById(reactionId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * This method will get all the usernames of the users who has reacted to the post, and return
   * them as a list.
   *
   * @param postId ID for the post.
   * @return A list of all the usernames.
   */
  public List<String> getUsernamesOfUsersWhoReactedToPost(int postId) {
    List<ReactionModel> reactions = getReactionByPostId(postId);
    List<String> userNames = new ArrayList<>();
    reactions.forEach(reaction -> userNames.add(
        userAccountService.getUserById(reaction.getUserId()).getUsername()));
    return userNames;
  }

  /**
   * This method will get all the usernames of the users who has reacted to the comment, and return
   * them as a list.
   *
   * @param commentId ID for the comment.
   * @return A list of all the usernames.
   */
  public List<String> getUsernamesOfUsersWhoReactedToComment(int commentId) {
    List<ReactionModel> reactions = getReactionByCommentId(commentId);
    List<String> userNames = new ArrayList<>();
    reactions.forEach(reaction -> userNames.add(
        userAccountService.getUserById(reaction.getUserId()).getUsername()));
    return userNames;
  }
}
