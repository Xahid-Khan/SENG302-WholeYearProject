package nz.ac.canterbury.seng302.portfolio.model.contract;

/**
 * A contract that represents a user's subscription to a group
 *
 * @param userId The id of the user
 * @param groupId The id of the group
 */
public record SubscriptionContract(
        int userId,
        int groupId
) implements Contractable {
}
