package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Config.SecretConstants;
import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Repository.DonaterRepository;
import com.kazankovorg.DonationManager.Repository.DonationRepository;
import com.kazankovorg.DonationManager.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DonationService {
    private final int limitOnPage = SecretConstants.LimitElementsOnPage;
    @Autowired
    private DonaterRepository donaterRepository;
    @Autowired
    private DonationRepository donationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CurrencyConvertService currencyConvertService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void notifyDonation(Donation donation) {
        simpMessagingTemplate.convertAndSend("/topic/donations", donation);
    }

    @Transactional
    public void addDonation(Long userId, String donaterName, Donation donation){
        donation.setAmount(currencyConvertService.convert(donation.getCurrency(), "RUB", donation.getAmount()));
        donation.setCurrency("RUB");

        if (donationRepository.existsById((long) donation.getId())) return;
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null){
            List<Donater> donaterList = donaterRepository.findByUserIdAndUsername(userId, donaterName);
            if (!donaterList.isEmpty()){
                donaterList.get(0).setUser(user);
                donaterRepository.save(donaterList.get(0));
                donation.setDonater(donaterList.get(0));
                donationRepository.save(donation);
            }
            else{
                Donater donater = new Donater();
                donater.setUser(user);
                donater.setUsername(donaterName);
                donaterRepository.save(donater);
                donation.setDonater(donater);
                donationRepository.save(donation);
            }
        }
        notifyDonation(donation);
    }

    public HashMap<String, Double> getTotalAmountByCurrencyAndDonaterId(Long donaterId) {
        List<Object[]> results = donationRepository.findTotalAmountByCurrencyAndDonaterId(donaterId);
        HashMap<String, Double> totalAmountByCurrency = new HashMap<>();
        for (Object[] result : results) {
            String currency = (String) result[0];
            Double totalAmount = (Double) result[1];
            totalAmountByCurrency.put(currency, totalAmount);
        }
        return totalAmountByCurrency;
    }

    public Long getCountOfDonationsByDonaterId(Long donaterId){
        return donationRepository.countDonationsByDonaterId(donaterId);
    }

    public Donation getDonationById(Long donationId){
        return donationRepository.findById(donationId).orElse(null);
    }

    public Page<Donation> getDonationListByDonaterId(Long donaterId, Integer page) {
        Pageable pageable = PageRequest.of(page, limitOnPage);
        return donationRepository.findByDonaterId(donaterId, pageable);
    }
}
