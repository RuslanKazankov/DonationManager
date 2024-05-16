package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findFirstByName(String name);
}
