package nz.ac.canterbury.seng302.portfolio.model.contract;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record CommentContract(
        @NotBlank(message = "User ID must be provided")
        int userId,
        @NotBlank(message = "Post ID must be provided")
        int postId,
        @Size(min = 1, max = 4096, message = "Comment length should be between 1 - 4096 characters")
        @NotBlank(message = "Cannot post an empty comment")
        String comment
) {
}
