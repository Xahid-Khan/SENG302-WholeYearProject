package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import java.time.Instant;
import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

/**
 * Denotes the most basic a group repository can be, I.E., before all calculations of additions are made.
 *
 * @param groupId The ID of the group associated
 */
public record BaseGroupRepositoryContract(
        Integer groupId
) implements Contractable {}
