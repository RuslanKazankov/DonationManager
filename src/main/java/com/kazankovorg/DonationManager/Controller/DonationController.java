package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    private DonaterService donaterService;
    @Autowired
    private UserService userService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private CentrifugeService centrifugeService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @GetMapping("/dalogin")
    public String daLogin(@RequestParam String code) throws IOException {
        String accessToken = daService.getAccessToken(code);
        userService.setTokenForCurrentUser(accessToken);

        if (userService.getSessionUser().getDatoken() != null)
            centrifugeService.userSub(
                    daService.getDaUser(userService.getSessionUser().getDatoken()).getId(),
                    userService.getSessionUser().getDatoken(),
                    daService.getDaUser(userService.getSessionUser().getDatoken()).getSocket_connection_token(),
                    userService.getSessionUser().getId()
            );
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
        if (sessionUser().getDatoken() == null)
            return "redirect:/profile";
        Integer pagesOfDonations = daService.getPagesOfDonations(userService.getSessionUser().getDatoken());
        if (page < 1 || pagesOfDonations < page)
            return "redirect:/donations";
        try {
            model.addAttribute("currentPage", page);
            List<Donation> donations = daService.getDonations(userService.getSessionUser().getDatoken(), page);
            if(donations.isEmpty()){
                model.addAttribute("pageCount", 1);
                return "donations";
            }
            model.addAttribute("donations", donations);
            model.addAttribute("pageCount", pagesOfDonations);
            List<Note> notes = noteService.getNotesFromToDonationId(
                    donations.get(Math.max(donations.size() - 1, 0)).getId(),
                    donations.get(0).getId());
            List<Note> releaseNotes = new ArrayList<>();
            List<String> statuses = new ArrayList<>();
            //Запросы в цикле, но цикл имеет константную величину
            for (var donation : donations){
                if (donation.getMessage() != null && donation.getMessage().length() > 300){
                    donation.setMessage("Это было тестовое чрезвычайно длинное сообщение.");
                }
                Note note = notes.stream()
                        .filter(n -> n.getDonation().getId() == donation.getId())
                        .findFirst().orElse(null);
                releaseNotes.add(note);
                statuses.add(donaterService.getStatusFromUserIdAndDonaterUsername(sessionUser().getId(), donation.getUsername()));
            }
            model.addAttribute("statuses", statuses);
            model.addAttribute("notes", releaseNotes);
        }
        catch (IOException e){
            model.addAttribute("Error", e.getMessage());
        }
        return "donations";
    }

}
