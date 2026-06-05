package com.bank.repository;

import com.bank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN u.emails e " +
            "LEFT JOIN u.phones p " +
            "WHERE (:name IS NULL OR u.name LIKE CONCAT(:name, '%')) " +
            "AND (:email IS NULL OR e.email = :email) " +
            "AND (:phone IS NULL OR p.phone = :phone) " +
            "AND (:dateOfBirth IS NULL OR u.dateOfBirth > :dateOfBirth)")
    Page<User> searchUsers(@Param("name") String name,
                           @Param("email") String email,
                           @Param("phone") String phone,
                           @Param("dateOfBirth") LocalDate dateOfBirth,
                           Pageable pageable);

    boolean existsByEmails_Email(String email);

    boolean existsByPhones_Phone(String phone);

    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.email = :login")
    Optional<User> findByEmail(@Param("login") String login);

    @Query("SELECT u FROM User u JOIN u.phones p WHERE p.phone = :login")
    Optional<User> findByPhone(@Param("login") String login);
}
