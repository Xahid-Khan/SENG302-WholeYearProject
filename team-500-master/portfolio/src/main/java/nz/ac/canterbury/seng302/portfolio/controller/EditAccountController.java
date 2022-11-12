package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.DTO.EditedUserValidation;
import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import nz.ac.canterbury.seng302.portfolio.mapping.UserMapper;
import nz.ac.canterbury.seng302.portfolio.service.AuthStateService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * This controller handles the edit_account endpoint, handling everything to do with updating the
 * users details.
 */
@Controller
public class EditAccountController extends AuthenticatedController {

  @Autowired private UserMapper userMapper;
  @Autowired private RegisterClientService registerClientService;
  @Autowired private SimpMessagingTemplate template;

  @Autowired
  public EditAccountController(
      AuthStateService authStateService, UserAccountService userAccountService) {
    super(authStateService, userAccountService);
  }

  /**
   * This method removes the left and right trailing white-spaces from the form data.
   *
   * @param binder a data binder from web-request
   */
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
  }

  /**
   * Gets the editing account page.
   *
   * @return the editing account page
   */
  @GetMapping(value = "/edit_account")
  public String getPage(Model model, @AuthenticationPrincipal PortfolioPrincipal principal) {
    // Adds a new User DTO to the page for editing purposes (Note: this can't be an override of User
    // or else type issues occur presumably due to the already bound model attribute)
    model.addAttribute("submittingUser", userMapper.userResponseToUserDto(getUser(principal)));
    return "edit_account";
  }

  /**
   * This controller receives a profile photo (file) and crops it to 1:1 and compress it to make
   * sure it's lower than 5mb. Then it used the gRPC protocols provided in registerClientService to
   * save the file in the DataBase in bytes (ByteString) format.
   *
   * @param submittingUser The submitted user
   * @param bindingResult An interface that extends errors
   * @param model HTML model DTO
   * @param principal An Authority State to verify user
   * @return either the edit account page if something goes wrong, or the 'my account page'
   *     otherwise
   */
  @PostMapping(value = "/edit_account")
  public String postPage(
      @ModelAttribute("submittingUser") @Validated(EditedUserValidation.class) User submittingUser,
      BindingResult bindingResult,
      Model model,
      @AuthenticationPrincipal PortfolioPrincipal principal) {

    if (bindingResult.hasErrors()) {
      return "edit_account";
    }
    try {
      // Update details using the new submitted user, and the current ID based on the token
      UserResponse currentUser = getUser(principal);
      if (!currentUser.getFirstName().equals(submittingUser.firstName()) || !currentUser.getLastName().equals(submittingUser.lastName()) || !currentUser.getNickname().equals(submittingUser.nickname())) {
        template.convertAndSend("/topic/groups", "name_change");
      }
      registerClientService.updateDetails(submittingUser, currentUser.getId());
    } catch (StatusRuntimeException e) {
      model.addAttribute("error", "Error connecting to Identity Provider...");
      return "edit_account";
    }

    return "redirect:my_account?edited=details";
  }
}
