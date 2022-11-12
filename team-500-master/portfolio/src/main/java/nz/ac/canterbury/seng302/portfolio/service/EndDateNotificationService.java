package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.GetPaginatedUsersOrderingElement;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class EndDateNotificationService {

    ArrayList<Timer> notificationQueue = new ArrayList<>();
    ArrayList<String> notificationIds = new ArrayList<>();

    @Autowired private UserAccountService userAccountService;

    @Autowired private NotificationService notificationService;

    public void addNotifications(Instant endDate, String eventType, String name, String id) {
        long timeUntilEndDate = ChronoUnit.MILLIS.between(Instant.now(), endDate);
        if (timeUntilEndDate < 0L ) {
            timeUntilEndDate = 0L;
        }
        PaginatedUsersResponse users = userAccountService.getPaginatedUsers(0, Integer.MAX_VALUE, GetPaginatedUsersOrderingElement.NAME, true);
        Timer endTimer = new Timer("endTimer");
        endTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String description = (List.of("Event", "Sprint", "Project").contains(eventType)) ? eventType + ": " + name + " has ended!" : eventType + ": " + name + " has occurred!";
                for (UserResponse user: users.getUsersList()) {
                    notificationService.create(new BaseNotificationContract(user.getId(), "Project", description));
                }
                notificationQueue.remove(endTimer);
                notificationIds.remove(eventType + id);
            }
        }, timeUntilEndDate);
        notificationQueue.add(endTimer);
        notificationIds.add(eventType + id);

        if (eventType.equals("Project") && timeUntilEndDate >= 604800000L) {
            Timer weekTimer = new Timer("weekTimer");
            weekTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    for (UserResponse user : users.getUsersList()) {
                        notificationService.create(new BaseNotificationContract(user.getId(), "Project", "Project ends in 1 week!"));
                    }
                    notificationQueue.remove(weekTimer);
                    notificationIds.remove(eventType + id);
                }
            }, timeUntilEndDate - 604800000L);
            notificationQueue.add(weekTimer);
            notificationIds.add(eventType + id);
        }

        if (timeUntilEndDate >= 86400000L) {
            Timer dayTimer = new Timer("dayTimer");
            dayTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String description = (List.of("Event", "Sprint", "Project").contains(eventType)) ? eventType + ": " + name + " ends in 1 day!" : eventType + ": " + name + " occurs in 1 day!";
                    for (UserResponse user : users.getUsersList()) {
                        notificationService.create(new BaseNotificationContract(user.getId(), "Project", description));
                    }
                    notificationQueue.remove(dayTimer);
                    notificationIds.remove(eventType + id);
                }
            }, timeUntilEndDate - 86400000L);
            notificationQueue.add(dayTimer);
            notificationIds.add(eventType + id);
        }

        if (timeUntilEndDate >= 3600000L) {
            Timer hourTimer = new Timer("hourTimer");
            hourTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    String description = (List.of("Event", "Sprint", "Project").contains(eventType)) ? eventType + ": " + name + " ends in 1 hour!" : eventType + ": " + name + " occurs in 1 hour!";
                    for (UserResponse user : users.getUsersList()) {
                        notificationService.create(new BaseNotificationContract(user.getId(), "Project", description));
                    }
                    notificationQueue.remove(hourTimer);
                    notificationIds.remove(eventType + id);
                }
            }, timeUntilEndDate - 3600000L);
            notificationQueue.add(hourTimer);
            notificationIds.add(eventType + id);
        }

    }

    public void removeNotifications(String id) {
        ArrayList<Timer> timersToRemove = new ArrayList<>();
        ArrayList<String> idsToRemove = new ArrayList<>();
        for (int i = 0; i < notificationIds.size(); i++) {
            if (notificationIds.get(i).equals(id)) {
                notificationQueue.get(i).cancel();
                timersToRemove.add(notificationQueue.get(i));
                idsToRemove.add(notificationIds.get(i));
            }
        }
        for (Timer timer: timersToRemove) {
            notificationQueue.remove(timer);
        }
        for (String notificationId: idsToRemove) {
            notificationIds.remove(notificationId);
        }
    }

}
