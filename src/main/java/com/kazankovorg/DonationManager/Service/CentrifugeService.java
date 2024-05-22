package com.kazankovorg.DonationManager.Service;

import com.kazankovorg.DonationManager.CentrifugeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class CentrifugeService {
    @Autowired
    private DonationService donationService;

    public void userSub(int daUserId, String access_token, String socket_token, Long userId){
        try{
            CentrifugeClient centrifugeClient = new CentrifugeClient(new URI(CentrifugeClient.getWEBSOCKET_URL()));
            centrifugeClient.init(access_token, socket_token, daUserId, donationService, userId);
            centrifugeClient.connect();
        }
        catch (java.net.URISyntaxException e){
            System.out.println(e.getMessage());
        }
    }
}
