package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.EventEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.MilestoneEntity;
import nz.ac.canterbury.seng302.portfolio.model.entity.SprintEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the library.
 */

public interface MilestoneRepository extends CrudRepository<MilestoneEntity, String> {}
