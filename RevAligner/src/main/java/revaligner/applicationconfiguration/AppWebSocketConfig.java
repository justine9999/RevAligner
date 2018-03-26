package revaligner.applicationconfiguration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
@EnableScheduling
public class AppWebSocketConfig
  extends AbstractWebSocketMessageBrokerConfigurer
{
  public void configureMessageBroker(MessageBrokerRegistry config)
  {
    config.enableSimpleBroker(new String[] { "/topic" });
    config.setApplicationDestinationPrefixes(new String[] { "/app" });
  }
  
  public void registerStompEndpoints(StompEndpointRegistry registry)
  {
    registry.addEndpoint(new String[] { "/chkalignprogress" }).withSockJS();
  }
}
