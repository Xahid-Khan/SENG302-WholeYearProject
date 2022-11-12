package nz.ac.canterbury.seng302.portfolio.repository;

import java.util.List;
import nz.ac.canterbury.seng302.portfolio.model.entity.ReactionModel;
import org.springframework.data.repository.CrudRepository;

public interface ReactionModelRepository extends CrudRepository<ReactionModel, Integer> {
  List<ReactionModel> getReactionsByUserId (int userId);
  List<ReactionModel> getReactionsByPostId (int postId);
  List<ReactionModel> getReactionsByCommentId (int commentId);
}
