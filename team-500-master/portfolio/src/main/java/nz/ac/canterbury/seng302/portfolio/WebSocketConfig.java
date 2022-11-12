package nz.ac.canterbury.seng302.portfolio;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class that enables and configures WebSockets with STOMP for this server.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Configures STOMP like so.
   *
   * <ol>
   *   <li>
   *     Expect <code>/app</code> to be prepended to all incoming messages.
   *     For a message to reach a method with <code>@MessageMapping("/ping")</code>, the client
   *     will need to send messages to <code>/app/ping</code>
   *   </li>
   *   <li>
   *      Filter so only messages targeted at <code>/topic/...</code> paths are emitted to clients
   *      of this socket. So eg. <code>@SendTo("/topic/abc")</code> will be sent, but
   *      <code>@SendTo("/internal/abc")</code> will not be sent to clients of this socket. <br/>
   *      Note that this doesn't modify the path sent to clients, so they should keep the
   *      <code>/topic</code> prefix in their subscriptions.
   *   </li>
   * </ol>
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config
            .setApplicationDestinationPrefixes("/app")
            .enableSimpleBroker("/topic")
    ;
  }

  /**
   * Configures /socket as the websocket path, and enables SockJS for that path, so clients using
   * SockJS can also connect.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry config) {
    config.addEndpoint("socket")
            .setAllowedOriginPatterns("https://*.canterbury.ac.nz")
            .withSockJS()
            .setClientLibraryUrl( "https://cdn.jsdelivr.net/sockjs/1.6.1/sockjs.min.js");
  }
}
