package nz.ac.canterbury.seng302.portfolio.service;

import java.util.List;
import nz.ac.canterbury.seng302.portfolio.mapping.SubscriptionMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.SubscriptionEntity;
import nz.ac.canterbury.seng302.portfolio.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles subscribing and unsubscribing people to and from groups
 */
@Service
public class SubscriptionService {

  @Autowired
  private SubscriptionRepository subscriptionRepository;

  @Autowired
  private SubscriptionMapper subscriptionMapper;

  /**
   * Creates a subscription in the database of a user to a group
   *
   * @param subscription SubscriptionContract containing the user and group
   */
  public void subscribe(SubscriptionContract subscription) {
    SubscriptionEntity result = subscriptionMapper.toEntity(subscription);
    subscriptionRepository.save(result);
  }

  /**
   * Removes a subscription in the database
   *
   * @param subscription SubscriptionContract containing the user and group
   */
  public void unsubscribe(SubscriptionContract subscription) {

    var data = subscriptionRepository.findByUserIdAndGroupId(subscription.userId(),
        subscription.groupId());
    subscriptionRepository.deleteById(data.getId());
  }

  /**
   * Retrieves a list of groupIds of the groups the user is subscribed to
   *
   * @return a list of groupIds of the groups the user is subscribed to
   */
  public List<Integer> getAllByUserId(int userId) {
    return subscriptionRepository.findByUserId(userId).stream().map(SubscriptionEntity::getGroupId)
        .toList();
  }

  public List<Integer> getAllByGroupId(int groupId) {
    return subscriptionRepository.findByGroupId(groupId).stream().map(SubscriptionEntity::getUserId)
        .toList();
  }
}
