package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.SubscriptionEntity;
import org.springframework.stereotype.Component;

/** Mapper for objects that represent subscriptions. */
@Component
public class SubscriptionMapper {

    /**
     * Converts a SubscriptionContract to a SubscriptionEntity
     * @param subscription a SubscriptionContract
     * @return an equivalent SubscriptionEntity
     */
    public SubscriptionEntity toEntity(SubscriptionContract subscription) {
        return new SubscriptionEntity(
                subscription.userId(),
                subscription.groupId()
        );
    }
}
