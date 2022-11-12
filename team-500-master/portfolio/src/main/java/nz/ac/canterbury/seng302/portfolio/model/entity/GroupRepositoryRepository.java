package nz.ac.canterbury.seng302.portfolio.model.entity;

import org.springframework.data.repository.CrudRepository;

/**
 * this interface extends the CRUD repository and makes use of the function/methods provided by the
 * library.
 */
public interface GroupRepositoryRepository extends CrudRepository<GroupRepositoryEntity, String> {
  boolean existsByGroupId (int groupId);

  GroupRepositoryEntity getAllByGroupId(int groupId);
}
