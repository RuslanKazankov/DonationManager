package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.DonationAlertsService;
import com.kazankovorg.DonationManager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {
    @Autowired
    UserService userService;

    @Autowired
    DonationAlertsService daService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @GetMapping("/")
    public String goHome(){

        return "index";
    }

    @GetMapping("/profile")
    public String goProfile(Model model){
        if (userService.getSessionUser().getDatoken() != null){
            model.addAttribute("daUser", daService.getDaUser(userService.getSessionUser().getDatoken()));
        }
        else
            model.addAttribute("daUrl", daService.getAuthorizationUrl());
        return "profile";
    }
}
