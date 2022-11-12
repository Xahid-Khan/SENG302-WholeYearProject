package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.PostModel;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.CommentService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.PostService;
import nz.ac.canterbury.seng302.portfolio.service.ReactionService;
import nz.ac.canterbury.seng302.portfolio.service.SubscriptionService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles the post and delete requests on the /subscribe endpoint.
 */
@Controller
@RequestMapping("/api/v1")
public class HomePageController extends AuthenticatedController {

  @Autowired
  private SubscriptionService subscriptionService;

  @Autowired
  private GroupsClientService groupsClientService;

  @Autowired
  private PostService postService;

  @Autowired
  private UserAccountService userAccountService;

  @Autowired
  private ReactionService reactionService;

  @Autowired
  private CommentService commentService;

  /**
   * This is similar to autowiring, but apparently recommended more than field injection.
   *
   * @param authStateService   an AuthStateService
   * @param userAccountService a UserAccountService
   */
  protected HomePageController(AuthStateService authStateService,
      UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * Handles post requests on the /subscribe endpoint to subscribe a user to a group.
   */
  @PostMapping(value = "/subscribe", produces = "application/json")
  public ResponseEntity<?> subscribe(@AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody SubscriptionContract subscription) {
    try {
      subscriptionService.subscribe(subscription);
      var result = subscriptionService.getAllByUserId(getUserId(principal));
      return ResponseEntity.ok().body(result);
    } catch (HttpMessageNotReadableException e) {
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Handles delete requests on the /subscribe endpoint to unsubscribe a user from a group.
   */
  @DeleteMapping(value = "/unsubscribe", produces = "application/json")
  public ResponseEntity<?> unsubscribe(@AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody SubscriptionContract subscription) {
    try {
      int userId = getUserId(principal);
      //Stops user from unsubscribing from a group if they are in it
      if (groupsClientService.isMemberOfTheGroup(userId, subscription.groupId())) {
        return ResponseEntity.badRequest().build();
      }

      subscriptionService.unsubscribe(subscription);
      var result = subscriptionService.getAllByUserId(userId);
      return ResponseEntity.ok().body(result);

    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/subscribe/{userId}", produces = "application/json")
  public ResponseEntity<?> getAll(@AuthenticationPrincipal PortfolioPrincipal principal,
      @PathVariable int userId) {
    try {
      var subscriptions = subscriptionService.getAllByUserId(userId);
      return ResponseEntity.ok(subscriptions);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping(value = "/posts", produces = "application/json")
  public ResponseEntity<?> getAllPosts(@AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestParam("offset")
      Optional<Integer> offset) {
    try {
      Integer userId = getUserId(principal);

      List<Integer> subscriptions = subscriptionService.getAllByUserId(userId);
      List<PostModel> posts = postService.getAllPostForMultipleGroups(subscriptions);

      var offsetValue = offset.orElse(0);
      var postSubset = posts.subList(Math.min((offsetValue) * 20, posts.size()),
          Math.min((offsetValue + 1) * 20, posts.size()));

      Map<String, Object> data = combineAndPrepareForFrontEnd(postSubset, userId);
      return ResponseEntity.ok(data);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get post by id.
   *
   * @param principal
   * @param postId
   * @return
   */
  @GetMapping(value = "/get_post/{postId}", produces = "application/json")
  public ResponseEntity<?> getPostById(
      @AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable int postId) {
    try {
      PostModel post = postService.getPostById(postId);
      Map<String, Object> filteredPosts = new HashMap<>();
      filteredPosts.put("postId", post.getId());
      filteredPosts.put("userId", post.getUserId());
      filteredPosts.put("username", userAccountService.getUserById(post.getUserId()).getUsername());
      filteredPosts.put("time", post.getCreated());
      filteredPosts.put("content", post.getPostContent());
      filteredPosts.put("reactions", reactionService.getUsernamesOfUsersWhoReactedToPost(
          post.getId()));
      filteredPosts.put("groupId", post.getGroupId());
      filteredPosts.put("comments", commentService.getCommentsForThePostAsJson(post.getId()));
      filteredPosts.put("groupName",
          groupsClientService.getGroupById(post.getGroupId()).getShortName());
      filteredPosts.put("isMember",
          groupsClientService.isMemberOfTheGroup(getUserId(principal), post.getGroupId()));

      return ResponseEntity.ok(filteredPosts);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }


  /**
   * This function creates a Map from posts to send it to the front end as JSON object.
   *
   * @param posts All the posts from a group as a List
   * @return A Hash Map where first element is string and second is an object.
   */
  private Map<String, Object> combineAndPrepareForFrontEnd(List<PostModel> posts, int userId) {
    Map<String, Object> postMap = new HashMap<>();

    List<Map<String, Object>> allPosts = new ArrayList<>();
    posts.forEach(post -> {
      Map<String, Object> filteredPosts = new HashMap<>();
      filteredPosts.put("postId", post.getId());
      filteredPosts.put("userId", post.getUserId());
      filteredPosts.put("username", userAccountService.getUserById(post.getUserId()).getUsername());
      filteredPosts.put("time", post.getCreated());
      filteredPosts.put("content", post.getPostContent());
      filteredPosts.put("reactions", reactionService.getUsernamesOfUsersWhoReactedToPost(
          post.getId()));
      filteredPosts.put("groupId", post.getGroupId());
      filteredPosts.put("comments", commentService.getCommentsForThePostAsJson(post.getId()));
      filteredPosts.put("groupName",
          groupsClientService.getGroupById(post.getGroupId()).getShortName());
      filteredPosts.put("isMember",
          groupsClientService.isMemberOfTheGroup(userId, post.getGroupId()));
      allPosts.add(filteredPosts);
    });
    postMap.put("posts", allPosts);
    return postMap;
  }
}
