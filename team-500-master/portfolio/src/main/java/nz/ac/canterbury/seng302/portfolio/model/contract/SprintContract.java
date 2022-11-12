package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;

/**
 * A sprint contract is a contract that is a sprint.
 *
 * @param projectId The id of the project the sprint is in
 * @param sprintId The id of the sprint
 * @param name The name of the sprint
 * @param description The description of the sprint
 * @param startDate The start date of the sprint
 * @param endDate The end date of the sprint
 * @param orderNumber The order number of the sprint
 */
public record SprintContract(
    String projectId,
    String sprintId,
    String name,
    String description,
    Instant startDate,
    Instant endDate,
    String colour,
    Long orderNumber  // Should only be present in responses
) implements Contractable {}