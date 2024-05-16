package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Note findByDonationId(int donation_id);
}
