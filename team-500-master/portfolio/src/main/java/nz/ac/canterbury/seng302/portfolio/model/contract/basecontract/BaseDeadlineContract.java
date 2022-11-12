package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a deadline can be, I.E., before all calculations of additions are made.
 *
 * @param name        the name of a deadline
 * @param description the description of a deadline
 * @param startDate   the due date of a deadline
 */
public record BaseDeadlineContract(
    String name,
    String description,
    Instant startDate
) implements Contractable {}
