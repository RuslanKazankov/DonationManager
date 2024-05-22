package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
    UserEntity findByUsername(String userName);
    UserEntity findFirstByUsername(String username);
    @Query("SELECT u FROM users u WHERE u.datoken IS NOT NULL")
    List<UserEntity> findAllUsersWithDatoken();
}
