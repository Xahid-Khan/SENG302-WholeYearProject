package nz.ac.canterbury.seng302.portfolio.model.contract;

import java.sql.Timestamp;

public record NotificationContract(
    String id,
    int userId,
    Timestamp timeNotified,
    String notifiedFrom,
    String description,
    boolean seen
) implements Contractable {}
