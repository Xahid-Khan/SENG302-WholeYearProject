package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a project can be, I.E., before all calculations of additions are made.
 *
 * @param name          the name of a project
 * @param description   the description of a project
 * @param startDate     the starting date of a project
 * @param endDate       the ending date of a project
 */
public record BaseProjectContract(
    String name,
    String description,
    Instant startDate,
    Instant endDate
) implements Contractable {}
