package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a milestone can be, I.E., before all calculations of additions are made.
 *
 * @param name          the name of a milestone
 * @param description   the description of a milestone
 * @param startDate     the date upon which the milestone occurs
 */
public record BaseMilestoneContract(
    String name,
    String description,
    Instant startDate
) implements Contractable {}