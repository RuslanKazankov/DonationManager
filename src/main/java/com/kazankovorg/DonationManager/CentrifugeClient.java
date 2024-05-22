package com.kazankovorg.DonationManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Service.DonationService;
import lombok.Getter;
import okhttp3.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CentrifugeClient extends WebSocketClient {
    private DonationService donationService;

    @Getter
    private static final String WEBSOCKET_URL = "wss://centrifugo.donationalerts.com/connection/websocket";
    private static final String SUBSCRIBE_URL = "https://www.donationalerts.com/api/v1/centrifuge/subscribe";
    private static final Gson gson = new Gson();
    private static final OkHttpClient httpClient = new OkHttpClient();

    private String access_token;
    private String socket_token;
    private int daUserId;
    private Long userId;

    public CentrifugeClient(URI serverUri) {
        super(serverUri);
    }

    public void init(String access_token, String socket_token, int daUserId, DonationService donationService, Long userId){
        this.access_token = access_token;
        this.socket_token = socket_token;
        this.daUserId = daUserId;
        this.donationService = donationService;
        this.userId = userId;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Opened connection");
        sendConnectionToken();
    }
    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        Map<String, Object> response = gson.fromJson(message, Map.class);

        // Проверка, является ли сообщение подтверждением соединения или подписки
        if (response.containsKey("result")) {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            if (result.containsKey("client")) {
                // Обработка сообщения подтверждения соединения
                String clientId = (String) result.get("client");
                try {
                    subscribeToChannels(clientId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (result.containsKey("channel") && result.containsKey("data")) {
                // Обработка сообщения о новом донате
                String channel = (String) result.get("channel");
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if(data.containsKey("seq"))
                    handleDonationMessage(channel, data);
            }
        }
    }

    private void handleDonationMessage(String channel, Map<String, Object> data)  {
        System.out.println("New donation on channel: " + channel);
        System.out.println("Donation data: " + data);

        Map<String, Object> donationMap = (Map<String, Object>) data.get("data");

        ObjectMapper mapper = new ObjectMapper();
        try {
            String donationJson = mapper.writeValueAsString(donationMap);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Donation donation = mapper.readValue(donationJson, Donation.class);

            donationService.addDonation(userId, donation.getUsername(), donation);
        }
        catch(com.fasterxml.jackson.core.JsonProcessingException e){
            System.out.println(e.getMessage());
        }

    }
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed connection with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex);
    }

    private void sendConnectionToken() {
        Map<String, Object> params = new HashMap<>();
        params.put("token", socket_token); // Replace with actual token

        Map<String, Object> message = new HashMap<>();
        message.put("params", params);
        message.put("id", 1);

        send(gson.toJson(message));
    }

    private void subscribeToChannels(String clientId) throws Exception {
        String[] channels = {"$alerts:donation_" + daUserId};

        HttpUrl.Builder urlBuilder = HttpUrl.parse(SUBSCRIBE_URL).newBuilder();
        urlBuilder.addQueryParameter("client", clientId);
        for (String channel : channels) {
            urlBuilder.addQueryParameter("channels", channel);
        }
        String url = urlBuilder.build().toString();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("client", clientId);
        requestBody.put("channels", channels);
        String jsonBody = gson.toJson(requestBody);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + access_token)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("Subscription response: " + responseBody);

            Map<String, Object> subscriptionResponse = gson.fromJson(responseBody, Map.class);
            if (subscriptionResponse.containsKey("channels")) {
                List<Map<String, String>> channelsList = (List<Map<String, String>>) subscriptionResponse.get("channels");
                for (Map<String, String> channelInfo : channelsList) {
                    String channel = channelInfo.get("channel");
                    String connectionToken = channelInfo.get("token");
                    connectToChannel(channel, connectionToken);
                }
            }
        }
    }

    private void connectToChannel(String channel, String connectionToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("channel", channel);
        params.put("token", connectionToken);

        Map<String, Object> message = new HashMap<>();
        message.put("params", params);
        message.put("method", 1);
        message.put("id", 2);

        send(gson.toJson(message));
    }
}
