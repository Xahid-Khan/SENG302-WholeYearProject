package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/** This is the repository for conversations. */
public interface ConversationRepository
    extends PagingAndSortingRepository<ConversationEntity, String> {

  /**
   * Paginates based on a list of user IDs. Note that this is primarily used by passing in a single
   *  user ID.
   *
   * @param userIds the user IDs to grab conversations for (Note that this will almost always just be a single user)
   * @param request the pagination request
   * @return a Page of ConversationEntities
   */
  Page<ConversationEntity> getPaginatedPostsByUserIdsIn(List<Integer> userIds, Pageable request);
}
