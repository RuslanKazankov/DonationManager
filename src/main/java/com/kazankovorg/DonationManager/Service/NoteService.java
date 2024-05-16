package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteService {
    @Autowired
    private NoteRepository noteRepository;

    public void saveNote(Note note){
        noteRepository.save(note);
    }

    public Note getNoteByDonationId(int donationId){
        return noteRepository.findByDonationId(donationId);
    }

    public void removeNote(int donationId){
        Note note = getNoteByDonationId(donationId);
        if (note != null)
            noteRepository.delete(note);
    }
}
