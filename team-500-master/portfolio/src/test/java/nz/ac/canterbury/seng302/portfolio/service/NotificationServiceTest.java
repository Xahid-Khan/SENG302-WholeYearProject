package nz.ac.canterbury.seng302.portfolio.service;
import nz.ac.canterbury.seng302.portfolio.mapping.NotificationMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.NotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.NotificationEntity;
import nz.ac.canterbury.seng302.portfolio.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the NotificationService class
 */
@SpringBootTest
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private LiveUpdatesService liveUpdatesService;

    @BeforeEach
    private void clear() {
        repository.deleteAll();
    }

    final int USER_ID = 1;
    final String NOTIFIED_FROM = "Shaylin";
    final String DESCRIPTION = "Shaylin high-fived your last post!";
    BaseNotificationContract contract = new BaseNotificationContract(USER_ID, NOTIFIED_FROM, DESCRIPTION);
    NotificationEntity entity = new NotificationEntity(USER_ID, NOTIFIED_FROM, DESCRIPTION);

    /**
     * Tests that a notification can be created and stored in the database
     */
//    @Test
//    public void createNotificationTest() {
//        Mockito.when(notificationMapper.toEntity(contract)).thenReturn(entity);
//        service.create(contract);
//        Mockito.verify(repository).save(entity);
//    }

    /**
     * Tests that all notifications are retrieved
     */
    @Test
    public void retrieveAllNotificationsForUserTest(){
        Mockito.when(notificationMapper.toContract(entity)).thenReturn(null);
        service.getAll(1);
        Mockito.verify(repository).findAllByUserIdOrderByTimeNotifiedDesc(1);
    }

    /**
     * Tests that when a list of notifications is created, they are added to the repository for all users
     */
//    @Test
//    public void createNotificationsForAllUsersTest() {
//        Mockito.when(notificationMapper.toEntity(contract)).thenReturn(entity);
//        Mockito.when(notificationMapper.toEntity(new BaseNotificationContract(USER_ID+1, NOTIFIED_FROM, DESCRIPTION))).thenReturn(new NotificationEntity(USER_ID+1, NOTIFIED_FROM, DESCRIPTION));
//        ArrayList<Integer> userIds = new ArrayList<>();
//        userIds.add(1);
//        userIds.add(2);
//        service.createForAllUsers(userIds, NOTIFIED_FROM, DESCRIPTION);
//        Mockito.verify(repository, Mockito.times(2)).save(Mockito.any());
//    }

    /**
     * Tests that when a user views notifications, all are marked as seen
     */
    @Test
    public void viewAllNotificationsSetsAsSeenTest() {
        ArrayList<NotificationEntity> entities = new ArrayList<>();
        entities.add(entity);
        entities.add(entity);
        Mockito.when(repository.findAllByUserIdOrderByTimeNotifiedDesc(1)).thenReturn(entities);
        service.setNotificationsSeen(1);
        Mockito.verify(repository, Mockito.times(2)).save(entity);
    }

}
