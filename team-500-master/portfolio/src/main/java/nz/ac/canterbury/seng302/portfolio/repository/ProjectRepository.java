package nz.ac.canterbury.seng302.portfolio.repository;

import nz.ac.canterbury.seng302.portfolio.model.entity.ProjectEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the library.
 */


public interface ProjectRepository extends CrudRepository<ProjectEntity, String> {}
