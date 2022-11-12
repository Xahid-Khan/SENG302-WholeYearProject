package nz.ac.canterbury.seng302.portfolio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
*   Service layer class to send live updates
*/
@Service
public class LiveUpdatesService {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendNotification() {
        template.convertAndSend("/topic/notification", "notification");
    }
}
