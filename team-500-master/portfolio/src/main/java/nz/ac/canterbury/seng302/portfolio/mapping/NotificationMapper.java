package nz.ac.canterbury.seng302.portfolio.mapping;

import nz.ac.canterbury.seng302.portfolio.model.contract.NotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.contract.basecontract.BaseNotificationContract;
import nz.ac.canterbury.seng302.portfolio.model.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper implements Mappable<NotificationEntity, BaseNotificationContract, NotificationContract> {


    @Override
    public NotificationEntity toEntity(BaseNotificationContract contract) {
        return new NotificationEntity(
                contract.userId(),
                contract.notifiedFrom(),
                contract.description()
        );
    }

    @Override
    public NotificationContract toContract(NotificationEntity entity) {
        return new NotificationContract(
                entity.getId(),
                entity.getUserId(),
                entity.getTimeNotified(),
                entity.getNotificationFrom(),
                entity.getDescription(),
                entity.getSeen()
        );
    }
}
