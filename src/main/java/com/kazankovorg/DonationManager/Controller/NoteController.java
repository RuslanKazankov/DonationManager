package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.DonationService;
import com.kazankovorg.DonationManager.Service.NoteService;
import com.kazankovorg.DonationManager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class NoteController {
    @Autowired
    private UserService userService;
    @Autowired
    private NoteService noteService;
    @Autowired
    private DonationService donationService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @GetMapping("/notes")
    public String getNotes(@RequestParam(defaultValue = "1") int page, Model model){
        Page<Note> notes = noteService.getNotes(sessionUser().getId(), page - 1);
        if (page > notes.getTotalPages())
            return "redirect:/notes";

        model.addAttribute("currentPage", page);
        model.addAttribute("pageCount", notes.getTotalPages());
        model.addAttribute("notes", notes.getContent());

        return "notes";
    }

    @PostMapping("/saveNote")
    @ResponseBody
    public ResponseEntity<String> saveNote(
            @RequestParam int donationId,
            @RequestParam String donationUsername,
            @RequestParam String donationMessage,
            @RequestParam String donationAmount,
            @RequestParam String donationCurrency,
            @RequestParam String donationCreatedAt,
            @RequestParam String noteText){
        Note note = noteService.getNoteByDonationId(donationId);
        if (note != null){
            note.setText(noteText);
            noteService.saveNote(note);
            return new ResponseEntity<>("Note changed successfully", HttpStatus.OK);
        }
        note = new Note();

        Donation donation = donationService.getDonationById((long) donationId);
        if (donation == null) {
            donation = new Donation();
            donation.setId(donationId);
            donation.setUsername(donationUsername);
            donation.setMessage(donationMessage);
            try {
                donation.setAmount(Double.parseDouble(donationAmount));
            } catch (NumberFormatException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
            donation.setCurrency(donationCurrency);
            donation.setCreated_at(donationCreatedAt);
        }
        note.setDonation(donation);
        note.setText(noteText);
        note.setUser(userService.getSessionUser());

        noteService.saveNote(note);
        return new ResponseEntity<>("Note added successfully", HttpStatus.OK);
    }
    @PostMapping("/removeNote")
    @ResponseBody
    public ResponseEntity<String> removeNote(@RequestParam int donationId){
        noteService.removeNote(donationId);
        return new ResponseEntity<>("Note deleted", HttpStatus.OK);
    }
}
