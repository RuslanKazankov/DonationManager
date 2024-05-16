package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Role;
import com.kazankovorg.DonationManager.Repository.RoleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role getUserRole(){
        if (roleRepository.findFirstByName("USER") == null){
            Role role = new Role();
            role.setName("USER");
            roleRepository.save(role);
        }
        return roleRepository.findFirstByName("USER");
    }
}
