package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.time.Instant;

/**
 * A contract for a deadline. Used for sending and retrieving sprints from the database
 *
 * @param groupId The id of the group associated with.
 * @param repositoryId Groups repository ID
 * @param token The token used to access the repository
 */
public record GroupRepositoryContract(
        Integer groupId,
        Integer repositoryId,
        String token,
        String alias,
        String longName
) implements Contractable {}