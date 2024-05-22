package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Role;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired RoleService roleService;
    @Transactional
    public boolean registerUser(UserEntity user)
    {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setRoles(new ArrayList<>(Collections.singletonList(roleService.getUserRole())));
        if (userRepository.findByUsername(user.getUsername()) != null)
            return false;
        userRepository.save(user);
        return true;
    }

    public UserEntity getSessionUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)){
            return userRepository.findFirstByUsername(authentication.getName());
        }
        return null;
    }

    public void setTokenForCurrentUser(String daToken){
        getSessionUser().setDatoken(daToken);
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }

    public List<UserEntity> getUsersWithDatoken(){
        return userRepository.findAllUsersWithDatoken();
    }
}
