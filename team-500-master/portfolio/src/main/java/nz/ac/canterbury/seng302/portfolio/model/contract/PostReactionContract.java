package nz.ac.canterbury.seng302.portfolio.model.contract;

import javax.validation.constraints.NotBlank;

public record PostReactionContract(
    @NotBlank
    int postId,

    @NotBlank
    int userId
) {

}
