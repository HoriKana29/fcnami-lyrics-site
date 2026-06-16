package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.User;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should save and find user by userIdentifier")
    void shouldSaveAndFindUserByIdentifier() {
        User user = TestFactory.createUser();

        userRepository.save(user);

        Optional<User> found =
                userRepository.findByUserIdentifier(user.getUserIdentifier());

        assertTrue(found.isPresent());
        assertEquals(user.getUserIdentifier(), found.get().getUserIdentifier());
    }
    @Test
    @DisplayName("should return true when userIdentifier exists")
    void shouldReturnTrueWhenUserExists() {
        User user = TestFactory.createUser();

        userRepository.save(user);

        boolean exists =
                userRepository.existsByUserIdentifier(user.getUserIdentifier());

        assertTrue(exists);
    }

    @Test
    @DisplayName("should return false when userIdentifier does not exist")
    void shouldReturnFalseWhenUserNotExists() {
        boolean exists =
                userRepository.existsByUserIdentifier("not_exist");

        assertFalse(exists);
    }

    // =========================
    // UNIQUE CONSTRAINT
    // =========================

    @Test
    @DisplayName("should not allow duplicate userIdentifier")
    void shouldNotAllowDuplicateUserIdentifier() {
        String identifier = "same_id";

        User user1 = TestFactory.createUser(identifier);
        User user2 = TestFactory.createUser(identifier);

        userRepository.save(user1);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(user2));
    }

    @Test
    @DisplayName("should return empty when userIdentifier not found")
    void shouldReturnEmptyWhenUserNotFound() {
        Optional<User> result =
                userRepository.findByUserIdentifier("not_exist");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenUserIdentifierIsNull() {
        User user = TestFactory.createUser();
        user.setUserIdentifier(null);

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user));
    }

    @Test
    void shouldFindCorrectUserAmongMany() {
        User u1 = TestFactory.createUser("id1");
        User u2 = TestFactory.createUser("id2");

        userRepository.save(u1);
        userRepository.save(u2);

        Optional<User> result =
                userRepository.findByUserIdentifier("id2");
        assertTrue(result.isPresent());
        assertEquals("id2", result.get().getUserIdentifier());
    }


}