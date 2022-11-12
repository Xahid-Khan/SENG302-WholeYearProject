package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;

/**
 * A contract for a deadline. Used for sending and retrieving sprints from the database
 *
 * @param projectId The id of the project this deadline is associated with.
 * @param name The name of the deadline.
 * @param description The description of the deadline.
 * @param startDate The start date of the deadline.
 */
public record DeadlineContract(
    String projectId,
    String deadlineId,
    String name,
    String description,
    Instant startDate,
    Long orderNumber
) implements Contractable {}