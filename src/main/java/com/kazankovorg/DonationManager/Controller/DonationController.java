package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
    @Autowired
    private DonationService donationService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @GetMapping("/dalogin")
    public String daLogin(@RequestParam String code) throws IOException {
        String accessToken = daService.getAccessToken(code);
        userService.setTokenForCurrentUser(accessToken);

        if (userService.getSessionUser().getDatoken() != null){
            centrifugeService.userSub(
                    daService.getDaUser(userService.getSessionUser().getDatoken()).getId(),
                    userService.getSessionUser().getDatoken(),
                    daService.getDaUser(userService.getSessionUser().getDatoken()).getSocket_connection_token(),
                    userService.getSessionUser().getId()
            );

            List<Donation> donations = daService.getLastDonations(sessionUser().getDatoken());
            for (var donation : donations){
                donationService.addDonation(sessionUser().getId(), donation.getUsername(), donation);
            }
        }
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
            //Запросы в цикле, но цикл имеет константную незначительную величину. C * n = n
            for (var donation : donations){
                if (donation.getMessage() != null && donation.getMessage().length() > 300){
                    donation.setMessage("Это было тестовое чрезвычайно длинное сообщение.");
                }
                Note note = notes.stream()
                        .filter(n -> n.getDonation().getId() == donation.getId())
                        .findFirst().orElse(null);
                releaseNotes.add(note);

                //Todo: statuses можно удалить, так как устанавливается донатер для доната, но тогда нужно изменить данные на форме.
                Donater donater = donaterService.getDonaterByUserIdAndDonaterName(sessionUser().getId(), donation.getUsername());
                if (donater != null)
                    statuses.add(donater.getStatus());
                else statuses.add(null);
                donation.setDonater(donater);
            }
            model.addAttribute("statuses", statuses);
            model.addAttribute("notes", releaseNotes);
        }
        catch (IOException e){
            model.addAttribute("Error", e.getMessage());
        }
        return "donations";
    }

    @MessageMapping("/donation")
    @SendTo("/topic/donations")
    public Donation send(Donation donation) {
        return donation;
    }
}
