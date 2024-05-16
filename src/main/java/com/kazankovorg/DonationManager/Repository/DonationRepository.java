package com.kazankovorg.DonationManager.Repository;

import com.kazankovorg.DonationManager.Models.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {

}
