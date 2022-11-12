package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.DeadlineEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the library.
 */

public interface DeadlineRepository extends CrudRepository<DeadlineEntity, String> {}
