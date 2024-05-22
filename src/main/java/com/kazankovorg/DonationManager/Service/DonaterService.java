package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Repository.DonaterRepository;
import com.kazankovorg.DonationManager.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DonaterService {
    private final int limitOnPage = 30;
    @Autowired
    private DonaterRepository donaterRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Donater> getTopDonaters(Long userId, Integer page){
        Pageable pageable = PageRequest.of(page, limitOnPage);
        return donaterRepository.findByUserIdOrderByTotalDonationAmount(userId, pageable);
    }

    public int getPagesOfDonaters(Long userId){
        int countOfDonaters = donaterRepository.countByUserId(userId);
        if (countOfDonaters == 0)
            return 1;
        return (countOfDonaters - 1) / limitOnPage + 1;
    }

    public String getStatusFromUserIdAndDonaterUsername(Long userId, String donaterName){
        var donaters = donaterRepository.findByUserIdAndUsername(userId, donaterName);
        if (donaters.isEmpty()) return "";
        return donaters.get(0).getStatus();
    }

    @Transactional
    public boolean setStatusByUserIdAndDonaterUsername(Long userId, String donaterName, String status){
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        List<Donater> donaters = donaterRepository.findByUserIdAndUsername(userId, donaterName);
        Donater donater;
        if (donaters.isEmpty()){
            donater = new Donater();
            donater.setUser(user);
            donater.setUsername(donaterName);
        }else{
            donater = donaters.get(0);
        }
        donater.setStatus(status);
        donaterRepository.save(donater);
        return true;
    }
}
