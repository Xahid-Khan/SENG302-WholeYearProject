package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.contract.CommentReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.PostReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseCommentReactionContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BasePostReactionContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.ReactionService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A CRUD controller for high-five reactions.
 */
@RestController
@RequestMapping("/group_feed")
public class ReactionController extends AuthenticatedController {

  @Autowired private ReactionService reactionService;

  public ReactionController(AuthStateService authStateService,
      UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * get the reactions for a given post using the post id.
   *
   * @param id Post id.
   * @return A Map of key username and a list of usernames as values.
   */
  @GetMapping(value = "/reaction_post/{id}", produces = "application/json")
  public ResponseEntity<Map<String, List<String>>> getReactionsForAPost(@PathVariable String id) {
    try {
      int postId = Integer.parseInt(id);
      List<String> userNames = reactionService.getUsernamesOfUsersWhoReactedToPost(postId);
      if (userNames.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      Map<String, List<String>> result = new HashMap<>();
      result.put("usernames", userNames);
      return ResponseEntity.ok().body(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * get the reactions for a given comment using the comment id.
   *
   * @param id Comment id.
   * @return A Map of key username and a list of usernames as values.
   */
  @GetMapping(value = "/reaction_comment/{id}", produces = "application/json")
  public ResponseEntity<Map<String, List<String>>> getReactionsForAComment(
      @AuthenticationPrincipal PortfolioPrincipal principal, @PathVariable String id) {
    try {
      int commentId = Integer.parseInt(id);
      List<String> userNames = reactionService.getUsernamesOfUsersWhoReactedToComment(commentId);
      if (userNames.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
      Map<String, List<String>> result = new HashMap<>();
      result.put("usernames", userNames);
      return ResponseEntity.ok().body(result);
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * This end-point process the request for adding a high-five reaction to the posts.
   *
   * @param principal        authentication principal.
   * @param reactionContract A contract that contains info about the reaction.
   * @return A ResponseEntity.
   */
  @PostMapping(value = "/post_high_five", produces = "application/json")
  public ResponseEntity<?> addPostHighFive(@AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody BasePostReactionContract reactionContract) {
    try {
      int userId = getUserId(principal);
      PostReactionContract postReactionContract = new PostReactionContract(
          reactionContract.postId(), userId);
      boolean response = reactionService.processPostHighFive(postReactionContract);
      if (response) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
      } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * This end-point process the request for adding a high-five reaction to the comment.
   *
   * @param principal        authentication principal.
   * @param reactionContract A contract that contains info about the reaction.
   * @return A ResponseEntity.
   */
  @PostMapping(value = "/comment_high_five", produces = "application/json")
  public ResponseEntity<?> addCommentHighFive(@AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestBody BaseCommentReactionContract reactionContract) {
    try {
      int userId = getUserId(principal);
      CommentReactionContract commentReactionContract = new CommentReactionContract(
          reactionContract.PostId(), userId, reactionContract.commentId());
      reactionService.processCommentHighFive(commentReactionContract);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError().build();
    }
  }
}
