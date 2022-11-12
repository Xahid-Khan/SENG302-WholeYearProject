package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic an event can be, I.E., before all calculations of additions are made.
 *
 * @param name          the name of an event
 * @param description   the description of an event
 * @param startDate     the starting date of an event
 * @param endDate       the ending date of an event
 */
public record BaseEventContract(
    String name,
    String description,
    Instant startDate,
    Instant endDate
) implements Contractable {}
