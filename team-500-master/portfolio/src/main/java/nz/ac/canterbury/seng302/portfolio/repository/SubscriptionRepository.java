package nz.ac.canterbury.seng302.portfolio.repository;

import java.util.ArrayList;
import nz.ac.canterbury.seng302.portfolio.model.entity.SubscriptionEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the
 * library.
 */
public interface SubscriptionRepository extends CrudRepository<SubscriptionEntity, Integer> {

  SubscriptionEntity findByUserIdAndGroupId(int user_id, int group_id);

  ArrayList<SubscriptionEntity> findByUserId(int user_id);

  ArrayList<SubscriptionEntity> findByGroupId(int group_id);

}
