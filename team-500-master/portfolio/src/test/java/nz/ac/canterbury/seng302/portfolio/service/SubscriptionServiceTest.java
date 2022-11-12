package nz.ac.canterbury.seng302.portfolio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.mapping.SubscriptionMapper;
import nz.ac.canterbury.seng302.portfolio.model.contract.SubscriptionContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.SubscriptionEntity;
import nz.ac.canterbury.seng302.portfolio.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests the functionality of SubscriptionService
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class SubscriptionServiceTest {
  private final int USER_ID = 1;
  private final int GROUP_ID = 1;
  private final SubscriptionContract contract = new SubscriptionContract(USER_ID, GROUP_ID);
  private final SubscriptionEntity entity = new SubscriptionEntity(USER_ID, GROUP_ID);
  @Mock
  private SubscriptionRepository subscriptionRepository;
  @InjectMocks
  private SubscriptionService subscriptionService;
  @Mock
  private SubscriptionMapper subscriptionMapper;

  @BeforeEach
  public void beforeEach() {
    subscriptionRepository.deleteAll();
  }

  /**
   * Tests that you can successfully subscribe to a group that you are not already subscribed to
   */
  @Test
  public void subscribe() {
    Mockito.when(subscriptionMapper.toEntity(contract)).thenReturn(entity);
    subscriptionService.subscribe(contract);
    Mockito.verify(subscriptionRepository, Mockito.times(1)).save(entity);
  }

  /**
   * Tests that you can successfully unsubscribe from a group, you are already subscribed to
   */
  @Test
  public void unsubscribe() {
    Mockito.when(subscriptionMapper.toEntity(contract)).thenReturn(entity);
    Mockito.when(
            subscriptionRepository.findByUserIdAndGroupId(entity.getUserId(), entity.getGroupId()))
        .thenReturn(entity);
    subscriptionService.subscribe(contract);
    subscriptionService.unsubscribe(contract);
    Mockito.verify(subscriptionRepository, Mockito.times(1)).deleteById(entity.getId());

  }

  /**
   * Tests that you can successfully retrieve all groups you are subscribed to
   */
  @Test
  public void getAllSubscribersForGroup() {
    Mockito.when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(
        new ArrayList<>(Arrays.asList(
            new SubscriptionEntity(USER_ID, 1),
            new SubscriptionEntity(USER_ID, 2),
            new SubscriptionEntity(USER_ID, 3)
        ))
    );
    List<Integer> groupIds = subscriptionService.getAllByUserId(USER_ID);
    Mockito.verify(subscriptionRepository).findByUserId(USER_ID);
    for (int i = 0; i < 3; i++) {
      assertEquals(i + 1, groupIds.get(i));
    }
  }

  /**
   * Tests that you can successfully retrieve all groups you are subscribed to
   */
  @Test
  public void getAllSubscriptionsFromGroup() {
    Mockito.when(subscriptionRepository.findByGroupId(GROUP_ID)).thenReturn(
        new ArrayList<>(Arrays.asList(
            new SubscriptionEntity(1, GROUP_ID),
            new SubscriptionEntity(2, GROUP_ID),
            new SubscriptionEntity(3, GROUP_ID)
        ))
    );
    List<Integer> userIds = subscriptionService.getAllByGroupId(GROUP_ID);
    Mockito.verify(subscriptionRepository).findByGroupId(GROUP_ID);
    for (int i = 0; i < 3; i++) {
      assertEquals(i + 1, userIds.get(i));
    }
  }
}
