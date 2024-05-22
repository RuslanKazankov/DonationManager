package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Repository.DonationRepository;
import com.kazankovorg.DonationManager.Repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        note.setDonation(null);
        if (note != null)
            noteRepository.delete(note);
    }

    public List<Note> getNotesFromToDonationId(int from, int to){
        return noteRepository.findNotesByDonationIdRange(from, to);
    }
}
