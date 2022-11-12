package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;

/**
 * A contract for an event. Used for sending and retrieving sprints from the database.
 *
 * @param projectId The id of the project this event is associated with.
 * @param name The name of the event.
 * @param description The description of the event.
 * @param startDate The start date of the event.
 * @param endDate The end date of the event.
 */
public record  EventContract(
    String projectId,
    String eventId,
    String name,
    String description,
    Instant startDate,
    Instant endDate,
    Long orderNumber
) implements Contractable {}
