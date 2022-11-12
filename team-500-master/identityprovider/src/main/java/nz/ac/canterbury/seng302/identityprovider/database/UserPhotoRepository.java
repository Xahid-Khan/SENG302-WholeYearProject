package nz.ac.canterbury.seng302.identityprovider.database;

import org.springframework.data.repository.CrudRepository;

public interface UserPhotoRepository extends CrudRepository<PhotoModel, Integer> {
}
