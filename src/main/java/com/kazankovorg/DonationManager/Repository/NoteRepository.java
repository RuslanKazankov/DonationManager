package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Note findByDonationId(int donation_id);
    @Query("SELECT n FROM notes n WHERE n.donation.id BETWEEN :startId AND :endId")
    List<Note> findNotesByDonationIdRange(@Param("startId") int startId, @Param("endId")  int endId);
}
