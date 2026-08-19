/* Database layer for the usere where you can save a user, find by id, delete a user and much more
*/
package com.gambingapp.gaminghub.repository;

import com.gambingapp.gaminghub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long>{
    Optional<User>findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
