package nz.ac.canterbury.seng302.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.mapping.NotificationMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.NotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.NotificationEntity;
import nz.ac.canterbury.seng302.portfolio.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
  @Autowired NotificationRepository repository;
  @Autowired NotificationMapper mapper;
  @Autowired private SimpMessagingTemplate template;

  @Autowired
  LiveUpdatesService liveUpdatesService;

  /**
   * Inserts a new notification into the database and returns the entry
   *
   * @param baseContract
   * @return
   */
  public NotificationContract create(BaseNotificationContract baseContract) {
    liveUpdatesService.sendNotification();
    NotificationEntity entity = repository.save(mapper.toEntity(baseContract));
    NotificationContract contract = mapper.toContract(entity);
    // Send to Websocket
    template.convertAndSend("/topic/notification", contract);
    return contract;
  }

  /**
   * Retrieves all the notification for a particular user
   *
   * @param userId The id of the user whose notification will be retrieved
   * @return An arraylist of the users notifications
   */
  public List<NotificationContract> getAll(int userId) {
    Iterable<NotificationEntity> entities =
        repository.findAllByUserIdOrderByTimeNotifiedDesc(userId);

    ArrayList<NotificationContract> contracts = new ArrayList<>();
    for (NotificationEntity entity : entities) {
      contracts.add(mapper.toContract(entity));
    }
    return contracts;
  }

  public void createForAllUsers(List<Integer> userIds, String fromLocation, String description) {
    for (Integer userId : userIds) {
      create(new BaseNotificationContract(userId, fromLocation, description));
    }
  }

  public void setNotificationsSeen(Integer userId) {
    Iterable<NotificationEntity> notifications =
        repository.findAllByUserIdOrderByTimeNotifiedDesc(userId);
    for (NotificationEntity notification : notifications) {
      notification.setSeen(true);
      repository.save(notification);
    }
  }
}
