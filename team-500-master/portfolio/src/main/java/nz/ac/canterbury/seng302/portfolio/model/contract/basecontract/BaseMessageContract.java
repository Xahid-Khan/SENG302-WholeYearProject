package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes what the frontend will send to the backend regarding creating or editing messages.
 *
 * @param messageContent the message's content
 * @param sentBy who the message was sent by
 */
public record BaseMessageContract(
    // Note that @NotEmpty is used here instead of @NotBlank.
    //  This is an intentional design decision to allow users to send whitespace, which can be
    //  useful in the event of perhaps, breaking up a large message. This keeps it consistent with
    //  most messaging applications.
    @NotEmpty(message = "Message must have content")
    @Size(min = 1, max = 4096, message = "Message length should be between 1 - 4096 characters")
    String messageContent,
    @NotNull(message = "User who sent the message's ID is required")
    Integer sentBy,
    @NotNull(message = "User who sent the message's name is required")
    String senderName
) implements Contractable {}
