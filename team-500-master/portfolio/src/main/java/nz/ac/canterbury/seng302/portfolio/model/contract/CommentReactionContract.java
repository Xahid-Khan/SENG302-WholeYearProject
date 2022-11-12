package nz.ac.canterbury.seng302.portfolio.model.contract;

import javax.validation.constraints.NotBlank;

public record CommentReactionContract(
    @NotBlank
    int postId,

    @NotBlank
    int userId,

    @NotBlank
    int commentId
) {

}
