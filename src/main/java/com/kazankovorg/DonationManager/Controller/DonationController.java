package com.kazankovorg.DonationManager.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.DonationAlertsService;
import com.kazankovorg.DonationManager.Service.DonationService;
import com.kazankovorg.DonationManager.Service.NoteService;
import com.kazankovorg.DonationManager.Service.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DonationController {

    @Autowired
    private DonationAlertsService daService;
    @Autowired
    private DonationService donationService;
    @Autowired
    private UserService userService;
    @Autowired
    private NoteService noteService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @GetMapping("/dalogin")
    public String daLogin(@RequestParam String code) throws IOException {
        String accessToken = daService.getAccessToken(code);
        userService.setTokenForCurrentUser(accessToken);
        return "redirect:/profile";
    }

    @GetMapping("/dalogout")
    public String daLogout(){
        UserEntity user = userService.getSessionUser();
        user.setDatoken(null);
        userService.saveUser(user);
        return "redirect:/profile";
    }

    @GetMapping("/donations")
    public String donationList(@RequestParam(value = "page", defaultValue = "1") Integer page, Model model){
        try {
            List<Donation> donations = daService.getDonations(userService.getSessionUser().getDatoken(), page);
            model.addAttribute("donations", donations);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageCount", daService.getPageCount(userService.getSessionUser().getDatoken()));
            List<Note> notes = new ArrayList<>();
            for (var donation : donations){
                notes.add(noteService.getNoteByDonationId(donation.getId()));
            }
            model.addAttribute("notes", notes);
        }
        catch (IOException e){
            model.addAttribute("Error", e.getMessage());
        }
        return "donations";
    }

}
