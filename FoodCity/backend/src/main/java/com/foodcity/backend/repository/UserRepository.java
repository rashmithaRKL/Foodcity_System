package com.foodcity.backend.repository;

import com.foodcity.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByEnabledTrue();

    List<User> findByRolesContaining(User.Role role);

    // Custom query to find users by role and enabled status
    List<User> findByRolesContainingAndEnabledTrue(User.Role role);

    // Custom query to find users by first name or last name containing the search term
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);

    // Delete user by username
    void deleteByUsername(String username);

    // Find users created between dates
    List<User> findByLastLoginDateBetween(String startDate, String endDate);
}