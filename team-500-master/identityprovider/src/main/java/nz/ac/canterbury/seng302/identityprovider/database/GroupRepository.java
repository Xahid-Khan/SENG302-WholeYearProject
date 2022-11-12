package nz.ac.canterbury.seng302.identityprovider.database;

import org.springframework.data.repository.CrudRepository;

/**
 * The repository used for storing groups.
 */
public interface GroupRepository extends CrudRepository<GroupModel, Integer> {
  GroupModel findByLongName(String longName);

  GroupModel findByShortName(String shortName);
}
