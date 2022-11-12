package nz.ac.canterbury.seng302.portfolio.controller;

import java.util.HashMap;
import java.util.Map;
import nz.ac.canterbury.seng302.portfolio.authentication.PortfolioPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Controller;

/** Controller for handling and dispatching live socket messages. */
@Controller
public class LiveUpdatesController {

  /**
   * STOMP endpoint that receives a message string and echos it back with metadata attached.
   */
  @MessageMapping("/alert")
  @SendTo("/topic/edit-project")
  public Map<String, String> alert(
      @AuthenticationPrincipal PreAuthenticatedAuthenticationToken principal, String alert) {
    var authState = (PortfolioPrincipal) principal.getPrincipal();
    Map<String, String> message = new HashMap<>();
    message.put("username", alert.split("~")[2]);
    message.put("action", alert.split("~")[1]);
    message.put("location", alert.split("~")[0]);
    message.put("name", authState.getName());
    return message;
  }

  /**
   * STOMP endpoints which handles notifications.
   *
   * @param principal authentication principal
   * @return a parsed message
   */
  @MessageMapping("/notification")
  @SendTo("/topic/notification")
  public String notify(
      @AuthenticationPrincipal PreAuthenticatedAuthenticationToken principal, String username) {
    return username;
  }
}
