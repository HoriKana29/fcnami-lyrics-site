package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User,Long> {

    // หา User
    Optional<User> findByUserIdentifier(String userIdentifier);

    boolean existsByUserIdentifier(String userIdentifier);
}
