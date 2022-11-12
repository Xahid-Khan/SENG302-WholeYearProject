package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.NotificationEntity;
import org.springframework.data.repository.CrudRepository;


/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the library.
 */
public interface NotificationRepository extends CrudRepository<NotificationEntity, String> {
    Iterable<NotificationEntity> findAllByUserIdOrderByTimeNotifiedDesc(int userId);
}
