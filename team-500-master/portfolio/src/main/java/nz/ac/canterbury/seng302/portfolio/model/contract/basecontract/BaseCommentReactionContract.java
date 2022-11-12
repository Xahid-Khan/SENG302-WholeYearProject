package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import javax.validation.constraints.NotBlank;

public record BaseCommentReactionContract(
    @NotBlank (message = "Reaction must belong to a post")
    int PostId,

    @NotBlank (message = "Reaction must contain a comment ID")
    int commentId
) {

}
