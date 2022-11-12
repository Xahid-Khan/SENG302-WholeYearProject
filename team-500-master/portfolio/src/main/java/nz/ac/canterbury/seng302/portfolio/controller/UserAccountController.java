package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nz.ac.canterbury.seng302.portfolio.mapping.UserMapper;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This controller handles everything related to displaying user accounts.
 */
@Controller
public class UserAccountController extends AuthenticatedController {
  @Autowired RegisterClientService registerClientService;

  @Autowired UserMapper mapper;

  @Autowired
  public UserAccountController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * The register client service. Gives the user the edit account page
   *  with their current information prefilled.
   *
   * @param model the view model
   * @param edited if the account was edited, and is being returned from that
   * @return the account details page
   */
  @GetMapping(value = "/my_account")
  public String getPage(
      Model model,
      @RequestParam Optional<String> edited) {
    // If editing occurs, the message should be displayed here.
    if (edited.isPresent()) {
      if (edited.get().equals("password")) {
        model.addAttribute("editMessage", "Password changed successfully");
      } else if (edited.get().equals("details")) {
        model.addAttribute("editMessage", "User details updated successfully");
      }
    }

    // Add this class as a delegate, such that formatting can be called.
    model.addAttribute("delegate", this);
    return "account_details";
  }

  /**
   * Converts a timestamp into a formatted "relative" date.
   *
   * @param dateCreated when the user was created
   * @return a formatted string
   */
  public String getFormattedDate(Timestamp dateCreated) {
    LocalDate date = Instant
        .ofEpochSecond(dateCreated.getSeconds(), dateCreated.getNanos())
        .atZone(ZoneId.of("Pacific/Auckland"))
        .toLocalDate();

    String dateString = date.format(DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG));

    long years = ChronoUnit.YEARS.between(date, LocalDate.now());
    long months = ChronoUnit.MONTHS.between(date, LocalDate.now()) % 12;
    long days = ChronoUnit.DAYS.between(date, LocalDate.now());

    return dateString
        + " ("
        + ((years == 0) ? "" : years + " years ")
        + ((months == 0) ? "" : months + " months ")
        + ((months != 0) ? "" : days + " days")
        + ")";
  }

  /**
   * This function is used by Thymeleaf whenever a list of roles must be displayed to the user.
   * It converts the roles into a human-readable list, seperated by commas if need be.
   *
   * @param roles a list of roles of the user
   * @return      the human friendly readable output of the roles
   */
  public String formatUserRoles(List<UserRole> roles) {
    return roles.stream().map(role -> switch (role) {
      case STUDENT -> "Student";
      case TEACHER -> "Teacher";
      case COURSE_ADMINISTRATOR -> "Course Administrator";
      default -> "Student";
    }).collect(Collectors.joining(", "));
  }
}
