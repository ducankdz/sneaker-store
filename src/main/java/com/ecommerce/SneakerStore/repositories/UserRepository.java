package com.ecommerce.SneakerStore.repositories;

import com.ecommerce.SneakerStore.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT COUNT(u) >0 FROM User u WHERE u.phoneNumber = :phoneNumber AND u.id <> :id")
    boolean existByPhoneNumberForOtherUsers(String phoneNumber,Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE MONTH(u.createdAt) = :month")
    int countUsersByMonth(int month);
}
