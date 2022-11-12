package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.ChangePasswordClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** This controller handles all password editing functionality. */
@Controller
public class EditPasswordController extends AuthenticatedController {

  @Autowired private ChangePasswordClientService changePasswordClientService;

  @Autowired
  public EditPasswordController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * Loads the editing password page.
   *
   * @return the password editing page
   */
  @GetMapping(value = "/edit_password")
  public String getPage() {
    return "edit_password";
  }

  /**
   * Handles submitting of the password form to update the password.
   *
   * @param model the model
   * @param principal the user's token
   * @param currentPassword the user's submitted current password
   * @param newPassword the user's submitted new password
   * @param confirmPassword the user's submitted password confirmation
   * @return the same page if an error occurs, or the account page otherwise
   */
  @PostMapping(value = "/edit_password")
  public String postPage(
      Model model,
      @AuthenticationPrincipal PortfolioPrincipal principal,
      @RequestParam(value = "currentPassword") String currentPassword,
      @RequestParam(value = "newPassword") String newPassword,
      @RequestParam(value = "confirmPassword") String confirmPassword) {

    if (newPassword.length() < 8 || newPassword.length() > 512) {
      model.addAttribute(
          "error", "New password must contain at least 8 and be no more than 512 characters");
      model.addAttribute("currentPass", currentPassword);
      model.addAttribute("newPass", newPassword);
      model.addAttribute("confirmPass", confirmPassword);
      return "edit_password";
    }

    if (!newPassword.matches("^(?!.* {2})(.*)")) {
      model.addAttribute("error", "New password cannot contain more than one whitespace in a row");
      model.addAttribute("currentPass", currentPassword);
      model.addAttribute("newPass", newPassword);
      model.addAttribute("confirmPass", confirmPassword);
      return "edit_password";
    }

    if (!newPassword.equals(confirmPassword)) {
      model.addAttribute("error", "New password does not match confirmed password");
      model.addAttribute("currentPass", currentPassword);
      model.addAttribute("newPass", newPassword);
      model.addAttribute("confirmPass", confirmPassword);
      return "edit_password";
    }

    if (newPassword.equals(currentPassword)) {
      model.addAttribute("error", "New password cannot be the same as current password");
      model.addAttribute("currentPass", currentPassword);
      model.addAttribute("newPass", newPassword);
      model.addAttribute("confirmPass", confirmPassword);
      return "edit_password";
    }

    try {
      int userId = getUserId(principal);
      ChangePasswordResponse response =
          changePasswordClientService.updatePassword(userId, currentPassword, newPassword);

      if (!response.getIsSuccess()) {
        model.addAttribute("error", response.getMessage());
        model.addAttribute("currentPass", currentPassword);
        model.addAttribute("newPass", newPassword);
        model.addAttribute("confirmPass", confirmPassword);
        return "edit_password";
      }

    } catch (StatusRuntimeException e) {
      model.addAttribute("error", "Error connecting to Identity Provider...");
      model.addAttribute("currentPass", currentPassword);
      model.addAttribute("newPass", newPassword);
      model.addAttribute("confirmPass", confirmPassword);
      return "edit_password";
    }
    return "redirect:/my_account?edited=password";
  }
}
