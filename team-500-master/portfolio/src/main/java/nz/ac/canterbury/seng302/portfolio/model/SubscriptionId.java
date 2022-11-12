package nz.ac.canterbury.seng302.portfolio.model;


import java.io.Serializable;

public class SubscriptionId implements Serializable {

    private int userId;

    private int groupId;

    public SubscriptionId() {
    }

    public SubscriptionId(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }
}
