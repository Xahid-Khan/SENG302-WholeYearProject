package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a sprint can be, I.E., before all calculations of additions are made.
 *
 * @param name          the name of a sprint
 * @param description   the description of a sprint
 * @param startDate     the starting date of a sprint
 * @param endDate       the ending date of a sprint
 * @param colour        the colour representation of a sprint
 */
public record BaseSprintContract(
    String name,
    String description,
    Instant startDate,
    Instant endDate,
    String colour
) implements Contractable {}
