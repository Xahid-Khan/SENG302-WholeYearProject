package nz.ac.canterbury.seng302.portfolio.model.contract;

import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * A contract relating for a group. Used for sending and retrieving groups from the database.
 *
 * @param id     the UID of the group
 * @param shortName   the short name of the group (e.g., "team 1000")
 * @param longName    the long name of the group (e.g., "Superstars")
 * @param users       the UID of all users currently belonging to that group
 */
public record GroupContract(
    Integer id,
    String shortName,
    String longName,
    String alias,
    Integer repositoryId,
    String token,
    boolean canEdit,
    ArrayList<UserContract> users,
//    GroupRepositoryContract repoInfo
    Object branches,
    Object commits
) implements Contractable {}
