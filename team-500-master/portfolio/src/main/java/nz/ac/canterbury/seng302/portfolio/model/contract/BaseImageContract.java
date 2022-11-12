package nz.ac.canterbury.seng302.portfolio.model.contract;

import javax.validation.constraints.NotBlank;

public record BaseImageContract(
        @NotBlank(message = "Image is required")
        String croppedImage
) implements Contractable {}
