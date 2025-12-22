package org.nikolic.programm.repositories;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.springframework.  data.jpa.repository.JpaRepository;
import org.springframework. data.jpa.repository.  Query;
import org.springframework.  data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Nur die absolut notwendigen Queries

    // Basic queries - funktionieren garantiert
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Role-based queries - funktionieren
    List<User> findByRole(UserRole role);

    // ✅ Active/Inactive mit @Query (wegen is_active Underscore)
    @Query("SELECT u FROM User u WHERE u.  is_active = true")
    List<User> findActiveUsers();

    @Query("SELECT u FROM User u WHERE u. is_active = false")
    List<User> findInactiveUsers();

    // ✅ Count queries - nur die wichtigsten
    @Query("SELECT COUNT(u) FROM User u WHERE u.is_active = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    // ✅ Admin check
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = 'ADMIN' AND u.is_active = true")
    boolean hasActiveAdmin();

    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.  is_active = true ORDER BY u.created_at ASC")
    Optional<User> findFirstAdmin();

    // ✅ Search functionality
    List<User> findByEmailContainingIgnoreCaseOrFullnameContainingIgnoreCase(String email, String fullname);
}