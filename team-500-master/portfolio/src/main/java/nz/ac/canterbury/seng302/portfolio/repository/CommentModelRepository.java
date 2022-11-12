package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.CommentModel;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

/**
 * CRUD Repository for a comment on a post
 */
public interface CommentModelRepository extends CrudRepository<CommentModel, Integer> {
    boolean deleteById (int commentId);

    List<CommentModel> findAllCommentByPostId(int postId);

    boolean deleteCommentsByPostId(int postId);
}
