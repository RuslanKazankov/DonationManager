package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DonationService {
    @Autowired
    DonationRepository donationRepository;
}
