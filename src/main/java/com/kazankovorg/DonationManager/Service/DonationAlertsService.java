package com.kazankovorg.DonationManager.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazankovorg.DonationManager.Models.DaUser;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Config.SecretConstants;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DonationAlertsService {
    private final int appId = SecretConstants.DonationAlertsAppId;
    private final String apiKey = SecretConstants.DonationAlertsApiKey;
    private final String redirectUrl = SecretConstants.DonationAlertsRedirectUrl;
    private final List<String> scopes = Arrays.asList("oauth-user-show", "oauth-donation-subscribe", "oauth-donation-index", "oauth-goal-subscribe", "oauth-poll-subscribe");

    private Header getAuthorizationHeader(String token){
        return new BasicHeader("Authorization", "Bearer " + token);
    }

    public String getAuthorizationUrl(){
        StringBuilder scopesStr = new StringBuilder();
        for (String scope : scopes){
            scopesStr.append(scope).append(" ");
        }
        scopesStr = new StringBuilder(scopesStr.toString().trim());
        return "https://www.donationalerts.com/oauth/authorize?" +
                "client_id=" + appId +
                "&redirect_uri=" + redirectUrl +
                "&response_type=code" +
                "&scope=" + scopesStr;
    }

    public String getAccessToken(String code) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost accessTokenRequest = new HttpPost("https://www.donationalerts.com/oauth/token");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("client_id", Integer.toString(appId)));
        params.add(new BasicNameValuePair("client_secret", apiKey));
        params.add(new BasicNameValuePair("redirect_uri", redirectUrl));
        params.add(new BasicNameValuePair("code", code));
        accessTokenRequest.setEntity(new UrlEncodedFormEntity(params));
        HttpEntity httpEntity = httpClient.execute(accessTokenRequest).getEntity();

        JSONObject accessToken = new JSONObject(EntityUtils.toString(httpEntity));

        httpClient.close();
        return accessToken.getString("access_token");
    }

    public List<Donation> getLastDonations(String token) throws IOException {
        return getDonations(token, 0);
    }

    public List<Donation> getDonations(String token, Integer page) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uri = new URIBuilder("https://www.donationalerts.com/api/v1/alerts/donations");
            if (page != 0){
                uri.addParameter("page", page.toString());
            }
            HttpGet donationsRequest = new HttpGet(uri.build());

            donationsRequest.addHeader(getAuthorizationHeader(token));
            HttpEntity responseEntity = httpClient.execute(donationsRequest).getEntity();

            JSONObject donations = new JSONObject(EntityUtils.toString(responseEntity));

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(donations.getJSONArray("data").toString(), new TypeReference<>() {});
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public Integer getPagesOfDonations(String token){
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet donationsRequest = new HttpGet(
                    new URIBuilder("https://www.donationalerts.com/api/v1/alerts/donations")
                            .addParameter("page", "1")
                            .build());
            donationsRequest.addHeader(getAuthorizationHeader(token));
            HttpEntity responseEntity = httpClient.execute(donationsRequest).getEntity();
            JSONObject meta = new JSONObject(EntityUtils.toString(responseEntity));
            return meta.getJSONObject("meta").getInt("last_page");
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DaUser getDaUser(String token){
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet userRequest = new HttpGet("https://www.donationalerts.com/api/v1/user/oauth");
            userRequest.addHeader(getAuthorizationHeader(token));
            HttpEntity responseEntity = httpClient.execute(userRequest).getEntity();
            JSONObject daUser = new JSONObject(EntityUtils.toString(responseEntity));
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(daUser.getJSONObject("data").toString(), DaUser.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public Donation getDonationFromJson(String donation) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        return mapper.readValue(donation, Donation.class);
//    }
}
