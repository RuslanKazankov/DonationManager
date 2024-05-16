package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(name = "error", required = false) String error, Model model) {
        if ("true".equals(error)) {
            model.addAttribute("ErrorMessage", "Неверный логин или пароль");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model){
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserEntity user, BindingResult result, Model model) {
        System.out.println(user.toString());
        if(result.hasErrors()){
            return "register";
        }
        if (!userService.registerUser(user)){
            model.addAttribute("ErrorMessage","Данный пользователь уже зарегестрирован");
            return "register";
        }
        return "redirect:/login";
    }

}
