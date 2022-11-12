package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes what the frontend will send to the backend regarding creating or editing conversations.
 *
 * @param userIds the list of user IDs who belong to the conversation
 */
public record BaseConversationContract(
    @NotEmpty(message = "User IDs must be provided")
    List<Integer> userIds
) implements Contractable {}