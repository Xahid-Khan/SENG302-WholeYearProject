package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;

/**
 * A contract for a milestone. Used for sending and retrieving sprints from the database.
 *
 * @param projectId The id of the project this event is associated with.
 * @param name The name of the milestone.
 * @param description The description of the milestone.
 * @param startDate The start date of the milestone.
 */
public record MilestoneContract(
    String projectId,
    String milestoneId,
    String name,
    String description,
    Instant startDate,
    Long orderNumber
) implements Contractable {}