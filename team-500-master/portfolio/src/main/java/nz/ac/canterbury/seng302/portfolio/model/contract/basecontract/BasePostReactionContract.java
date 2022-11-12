package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import javax.validation.constraints.NotBlank;

public record BasePostReactionContract(
    @NotBlank
    int postId
) {

}
