package nz.ac.canterbury.seng302.portfolio.DTO;

import com.google.protobuf.Timestamp;
import javax.annotation.Nullable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * A user in the context of the portfolio is used for validation and transportation to the IDP.
 * This record stores all attributes about the user, validates them using Javax.validation, and
 *  sends them off to the IDP to put in the database.
 *
 *  New features as of 16/05/2022 now use react with custom validation classes. Javax.validation is no longer used, but
 *  as it still functions here and would consume time to update this old feature, we have decided to leave it until it
 *  needs updating in the future.
 */
public record User(
    @Pattern(regexp = "[\\p{L}\\p{N}]*", message = "Username must only contain alphabetical characters or numbers, with no spaces", groups = RegisteredUserValidation.class)
    @NotBlank(message = "Username is required", groups = RegisteredUserValidation.class)
    @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters, and cannot contain any spaces", groups = RegisteredUserValidation.class)
    String username,

    @Pattern(regexp = "^(?!.*  )(.*)", message = "Password cannot contain more than one whitespace in a row", groups = RegisteredUserValidation.class)
    @NotBlank(message = "Password is required", groups = RegisteredUserValidation.class)
    @Size(min = 8, max = 512, message = "Password must be at least 8 characters long and no longer than 512 characters", groups = RegisteredUserValidation.class)
    String password,

    @Pattern(regexp = "^(?=^[\\p{L}]?)(?!^['-/ ])(?!.*['-/]{2})(?!.* {2})([\\p{L} '/-]*)", message = "Name must only contain alphabetical characters, or special characters: \"/\", \"-\", or \"'\". There must not be two special characters in a row.", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @NotBlank(message = "First name is required", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Size(max = 50, message = "First name cannot be longer than 50 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    String firstName,

    @Pattern(regexp = "^(?=^[\\p{L}]?)(?!^['-/ ])(?!.*['-/]{2})(?!.* {2})([\\p{L} '/-]*)", message = "Name must only contain alphabetical characters, or special characters: \"/\", \"-\", or \"'\". There must not be two special characters in a row.", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Size(max = 50, message = "Middle name(s) cannot be longer than 50 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Nullable String middleName,

    @Pattern(regexp = "^(?=^[\\p{L}]?)(?!^['-/ ])(?!.*['-/]{2})(?!.* {2})([\\p{L} '/-]*)", message = "Name must only contain alphabetical characters, or special characters: \"/\", \"-\", or \"'\". There must not be two special characters in a row.", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @NotBlank(message = "Last name is required", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Size(max = 50, message = "Last name cannot be longer than 50 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    String lastName,

    @Pattern(regexp = "^(?=^[\\p{L}]?)(?!^['-/ ])(?!.*['-/]{2})(?!.* {2})([\\p{L} '/-]*)", message = "Name must only contain alphabetical characters, or special characters: \"/\", \"-\", or \"'\". There must not be two special characters in a row.", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Size(max = 32, message = "Nickname cannot be longer than 32 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Nullable String nickname,

    @Size(max = 512, message = "Bio cannot be longer than 512 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Nullable String bio,

    @Pattern(regexp = "\\p{L}+/\\p{L}+|", message = "Personal pronouns must be two alphabetical pronouns, seperated by a forward slash (/) e.g. they/them", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Size(max = 50, message = "Personal pronouns cannot be longer than 50 characters", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    @Nullable String personalPronouns,

    // Emails are stupidly complicated. This basic Regex should suffice for most use cases, however.
    @Email(message = "Email cannot contain any special characters, be of the format recipient@domain.suffix, and must have a valid domain name ", regexp = "[0-9A-Za-z-_.]+@[0-9A-Za-z-_.]+.[A-Za-z]+", groups = {EditedUserValidation.class, RegisteredUserValidation.class})
    String email,

    Timestamp created
    ) {
  /**
   * Canonical constructor to ensure that all nulls are instead filled with empty strings.
   * Null safety is important :)
   *
   * @param username      The user's username
   * @param password      The user's password
   * @param firstName     The user's first name
   * @param middleName    The user's middle name(s)
   * @param lastName      The user's last name
   * @param nickname      The user's nickname
   * @param bio           The user's bio
   * @param personalPronouns      The user's pronouns
   * @param email         The user's email
   */
  public User(
      String username,
      String password,
      String firstName,
      String middleName,
      String lastName,
      String nickname,
      String bio,
      String personalPronouns,
      String email,
      Timestamp created
  ) {
    // Required values
    this.username = username;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    // Optional values
    this.middleName = middleName == null ? "" : middleName;
    this.nickname = nickname == null ? "" : nickname;
    this.bio = bio == null ? "" : bio;
    this.personalPronouns = personalPronouns == null ? "" : personalPronouns;
    //generated
    this.created = created;
  }
}
