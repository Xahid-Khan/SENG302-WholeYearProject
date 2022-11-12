package nz.ac.canterbury.seng302.identityprovider.database;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserModel, Integer> {

    UserModel findByUsername(String username);

    UserModel findByEmail(String email);

    UserModel findById(int id);
}
