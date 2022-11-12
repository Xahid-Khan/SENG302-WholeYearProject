package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record BasePostContract(
    @Size(min = 1, max = 4096, message = "Post content should be between 1 and 4096 characters")
    @NotBlank(message = "Post Cannot be empty")
    String postContent
) implements Contractable {}
