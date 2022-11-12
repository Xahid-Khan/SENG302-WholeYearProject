package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.repository.PostModelRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * This is helper service for post to help the CRUD functionality.
 */
@Service
public class PostService {

  @Autowired
  private PostModelRepository postRepository;

  @Autowired
  private GroupsClientService groupsClientService;

  @Autowired
  private SubscriptionService subscriptionService;

  @Autowired
  private CommentService commentService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  private SimpMessagingTemplate template;


  /**
   * This method will get all the posts in the database.
   *
   * @return A list of postModels.
   */
  public List<PostModel> getAllPosts() {
    try {
      return (List<PostModel>) postRepository.findAll();
    } catch (NoSuchElementException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  /**
   * This method will get all the post for a given group, using the group Id.
   *
   * @param groupId A group ID of type integer.
   * @return A list of post models.
   */
  public List<PostModel> getAllPostsForAGroup(int groupId) {
    return postRepository.findPostModelByGroupId(groupId);
  }


  /**
   * Handles pagination using PageRequest.of, taking into account a group ID.
   *
   * @param groupId The group id
   * @param page    which page of the data to load (I.E., 0 will load 0 - limit)
   * @param limit   limit of posts to grab. Must be greater than 0
   * @return the specified posts based on the parameters given
   */
  public Page<PostModel> getPaginatedPostsForGroup(int groupId, int page, int limit) {
    Pageable request = PageRequest.of(page, limit, Sort.by("created").descending());
    return postRepository.getPaginatedPostsByGroupId(groupId, request);
  }

  /**
   * Handles pagination using PageRequest.of.
   *
   * @param page  which page of the data to load (I.E., 0 will load 0 - limit)
   * @param limit limit of posts to grab. Must be greater than 0
   * @return the specified posts based on the parameters given
   */
  public Page<PostModel> getPaginatedPosts(int page, int limit) {
    Pageable request = PageRequest.of(page, limit, Sort.by("created").descending());
    return postRepository.findAll(request);
  }

  /**
   * This method/function will return the posts user has subscribed to. All teh posts are in
   * descending order by created time.
   *
   * @param groupIds A list of user IDs
   * @return A list of Post Models
   */
  public List<PostModel> getAllPostForMultipleGroups(List<Integer> groupIds) {
    return postRepository.findPostModelByGroupIdOrderByCreatedDesc(groupIds);
  }

  /**
   * This function will create new instance of the post and save it in the database.
   *
   * @param newPost A post contract containing groupId and contents of the post.
   * @param userId  Integer (Id of the user who made the post)
   * @return True if successful false otherwise.
   */
  public boolean createPost(PostContract newPost, int userId) {
    if (newPost.postContent().length() == 0) {
      return false;
    }
    try {
      PostModel postModel = new PostModel(newPost.groupId(), userId, newPost.postContent());
      postRepository.save(postModel);

      //Gets details for notification
      GroupDetailsResponse groupDetails = groupsClientService.getGroupById(newPost.groupId());

      List<Integer> userIds = subscriptionService.getAllByGroupId(newPost.groupId());
      String posterUsername = userAccountService.getUserById(userId).getUsername();
      String groupName = groupDetails.getShortName();

      template.convertAndSend("/topic/posts", userIds);

      // Send notification to all members of the group
      for (Integer otherUserId : userIds) {
        if (otherUserId != userId) {
          notificationService.create(new BaseNotificationContract(otherUserId, "Your Subscriptions",
              posterUsername + " created a post in " + groupName + "!"));
        }
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  /**
   * This function will delete the post by using the postId.
   *
   * @param postId Integer Post ID
   * @return True if deletion is successful False otherwise
   */
  public boolean deletePost(int postId) {
    try {
      var postFound = postRepository.findById(postId);
      if (postFound.isPresent()) {
        var comments = commentService.getCommentsForGivenPost(postId);
        postRepository.deleteById(postId);
        if (!comments.isEmpty()) {
          commentService.getCommentsForGivenPost(postId);
        }
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This function will update the changes made to the post.
   *
   * @param updatedPost A PostContract
   * @param postId      Integer ID of the Post
   * @return True if update is successful False otherwise.
   */
  public boolean updatePost(PostContract updatedPost, int postId) {
    try {
      var post = postRepository.findById(postId).orElseThrow();
      post.setPostContent(updatedPost.postContent());
      postRepository.save(post);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * This function will get a specific post using Post ID and return it.
   *
   * @param postId Integer Post ID
   * @return Returns PostModel if found, null otherwise.
   */
  public PostModel getPostById(int postId) {
    try {
      return postRepository.findById(postId).orElseThrow();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This method will delete all the posts made by a group when a group is deleted.
   *
   * @param groupId An integer
   */
  public void deleteAllPostWithGroupId(int groupId) {
    var data = postRepository.findPostModelByGroupId(groupId);
    for (PostModel post : data) {
      commentService.deleteAllCommentByPostId(post.getId());
      postRepository.deleteById(post.getId());
    }
  }
}
