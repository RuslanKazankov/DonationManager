package com.kazankovorg.DonationManager.Config;

import com.kazankovorg.DonationManager.Models.DaUser;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.CentrifugeService;
import com.kazankovorg.DonationManager.Service.DonationAlertsService;
import com.kazankovorg.DonationManager.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CentrifugeStartRunner implements ApplicationRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private DonationAlertsService daService;

    @Autowired
    private CentrifugeService centrifugeService;

    @Override
    public void run(ApplicationArguments args) {
        List<UserEntity> users = userService.getUsersWithDatoken();
        for (var user : users){
            DaUser daUser = daService.getDaUser(user.getDatoken());
            centrifugeService.userSub(
                    daUser.getId(),
                    user.getDatoken(),
                    daUser.getSocket_connection_token(),
                    user.getId()
            );
        }
    }
}