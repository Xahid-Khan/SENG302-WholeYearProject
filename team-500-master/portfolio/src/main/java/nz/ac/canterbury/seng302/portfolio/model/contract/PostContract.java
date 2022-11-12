package nz.ac.canterbury.seng302.portfolio.model.contract;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record PostContract (
        @NotBlank(message = "Must provide a valid Group ID")
        int groupId,

        @Size(min = 1, max = 4096, message = "Post content should be between 1 and 4096 characters")
        @NotBlank(message = "Post Cannot be empty")
        String postContent
) {
}
