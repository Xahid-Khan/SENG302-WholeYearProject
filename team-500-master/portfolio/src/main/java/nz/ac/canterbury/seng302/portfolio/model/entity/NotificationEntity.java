package nz.ac.canterbury.seng302.portfolio.model.entity;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/** Entity class for notifications. */
@Entity
@Table(name = "notification")
public class NotificationEntity extends PortfolioEntity {

    @Column(name = "user_id", nullable = false)
    private int userId;

    @CreationTimestamp
    @Column(name = "time_notified")
    private Timestamp timeNotified;

    @Column(name = "notification_from")
    private String notificationFrom;

    @Column(name = "description")
    private String description;

    @Column(name = "seen", nullable = false, columnDefinition = "boolean default false")
    private boolean seen;

    protected NotificationEntity() {

    }

    public NotificationEntity(int userId, String from, String description) {
        this.userId = userId;
        this.notificationFrom = from;
        this.description = description;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotificationFrom() {
        return notificationFrom;
    }

    public void setNotificationFrom(String from) {
        this.notificationFrom = from;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getTimeNotified() {
        return timeNotified;
    }

    public void setTimeNotified(Timestamp timeNotified) {
        this.timeNotified = timeNotified;
    }
}