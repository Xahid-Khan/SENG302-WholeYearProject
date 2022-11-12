package nz.ac.canterbury.seng302.portfolio.model.contract.basecontract;

import nz.ac.canterbury.seng302.portfolio.model.contract.Contractable;

public record BaseNotificationContract (
    int userId,
    String notifiedFrom,
    String description
) implements Contractable {}
