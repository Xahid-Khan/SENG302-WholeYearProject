package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a group can be, I.E., before all calculations of additions are made.
 * All simple validations are defined here.
 *
 * @param shortName   the short name of the group (e.g., "team 1000")
 * @param longName    the long name of the group (e.g., "Superstars")
 */
public record BaseGroupContract(
    @Size(max = 64, message = "Long name must not exceed 64 characters")
    @NotBlank(message = "Long name is required")
    String longName,
    @Size(max = 32, message = "Short name must not exceed 32 characters")
    @NotBlank(message = "Short name is required")
    String shortName
) implements Contractable {}
