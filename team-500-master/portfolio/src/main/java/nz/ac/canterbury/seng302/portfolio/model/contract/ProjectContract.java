package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;
import java.util.List;

/**
 * A contract for a project.
 *
 * @param id The id of the project.
 * @param name The name of the project.
 * @param description The description of the project.
 * @param startDate The start date of the project.
 * @param endDate The end date of the project.
 * @param sprints The sprints of the project.
 * @param events The events of the project.
 */
public record ProjectContract(
    String id,
    String name,
    String description,
    Instant startDate,
    Instant endDate,
    List<SprintContract> sprints,
    List<EventContract> events,
    List<MilestoneContract> milestones,
    List<DeadlineContract> deadlines
) implements Contractable {}
