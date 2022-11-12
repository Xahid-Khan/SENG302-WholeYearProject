package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.util.List;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * A contract for a user.
 *
 * @param firstName           the first name of the user
 * @param middleName          the middle name(s) of the user
 * @param lastName            the last name of the user
 * @param nickName            the nickname of the user
 * @param username            the username of the user
 * @param email               the email of the user
 * @param personalPronouns    the personal pronouns of the user
 * @param bio                 the bio of the user
 * @param roles               the roles of the user
 */
public record UserContract(
    Integer id,
    String firstName,
    String middleName,
    String lastName,
    String nickName,
    String username,
    String email,
    String personalPronouns,
    String bio,
    List<UserRole> roles
) implements Contractable {}
