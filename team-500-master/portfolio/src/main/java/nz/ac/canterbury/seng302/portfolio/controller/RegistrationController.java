package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.portfolio.DTO.RegisteredUserValidation;
import nz.ac.canterbury.seng302.portfolio.DTO.User;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.GroupsClientService;
import nz.ac.canterbury.seng302.portfolio.service.RegisterClientService;
import nz.ac.canterbury.seng302.portfolio.service.SubscriptionService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Handles the /register endpoint for either GET requests or POST requests. */
@Controller
public class RegistrationController {
  @Autowired
  private GroupsClientService groupsClientService;

  @Autowired
  private SubscriptionService subscriptionService;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(false));
  }

  /**
   * Calling the /register endpoint with a GET request will return the user a form to fill out for
   * registration.
   *
   * @param model Adds a blank user for the user to fill in
   * @return The registration_form thymeleaf page
   */
  @GetMapping(value = "/register")
  public String registerForm(Model model) {
    if (!model.containsAttribute("user")) {
      model.addAttribute("user", new User("", "", "", "", "", "", "", "", "", null));
    }
    return "registration_form";
  }

  @Autowired private RegisterClientService registerClientService;

  @Autowired private AuthenticateClientService authenticateClientService;

  /**
   * Calling the /register endpoint with a POST request will validate the user, and send the user to
   * the RegisterClientService to be sent up for database validation and saving. If an error occurs
   * along the way, it will be caught here. If all is successful, the registered thymeleaf page will
   * be loaded.
   *
   * @param user The user passed to the controller
   * @param bindingResult The result of validation on the user
   * @param model The model to update for errors for thymeleaf
   * @return The registration_form thymeleaf page if unsuccessful, the registered thymeleaf page
   *     otherwise.
   */
  @PostMapping("/register")
  public String register(
      @ModelAttribute @Validated(RegisteredUserValidation.class) User user,
      BindingResult bindingResult,
      Model model,
      HttpServletRequest request,
      HttpServletResponse response,
      RedirectAttributes redirectAttributes) {
    // If there are errors in the validation of the user
    if (bindingResult.hasErrors()) {
      // Allows the bindingResult (errors) and user fields to persist through the redirect
      redirectAttributes.addFlashAttribute(
          "org.springframework.validation.BindingResult.user", bindingResult);
      model.addAttribute("user", user);
      return "registration_form";
    }

    UserRegisterResponse registerReply;
    try {
      registerReply = registerClientService.register(user);
      model.addAttribute("registerMessage", registerReply.getMessage());

      if (!registerReply.getIsSuccess()) {
        return "registration_form";
      }
    } catch (StatusRuntimeException e) {
      model.addAttribute("registerMessage", "Error connecting to Identity Provider...");
      return "registration_form";
    }

    // Logs the user in
    AuthenticateResponse loginReply;
    try {
      loginReply = authenticateClientService.authenticate(user.username(), user.password());
    } catch (StatusRuntimeException e) {
      model.addAttribute("error", "Error connecting to Identity Provider...");
      return "redirect:login";
    }

    if (loginReply.getSuccess()) {
      var domain = request.getHeader("host");
      CookieUtil.create(
          response,
          "lens-session-token",
          loginReply.getToken(),
          true,
          5 * 60 * 60, // Expires in 5 hours
          domain.startsWith("localhost") ? null : domain);
      // Redirect user if login succeeds
      // redirectAttributes.addFlashAttribute("message", "Successfully logged in.");

      var group = groupsClientService.getGroupById(2);
      var userList = group.getMembersList().stream().map(member -> member.getId()).collect(
          Collectors.toList());
      var userId = (int) loginReply.getUserId();
      if (userList.contains(userId)) {
        subscriptionService.subscribe(new SubscriptionContract(userId, group.getGroupId()));
      }

      return "redirect:my_account";
    }

    return "redirect:login"; // return the template in templates folder
  }
}
