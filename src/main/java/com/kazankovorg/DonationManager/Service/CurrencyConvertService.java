package com.kazankovorg.DonationManager.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kazankovorg.DonationManager.Config.SecretConstants;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyConvertService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_KEY = "currency_rates";
    private static final String TIMESTAMP_KEY = "currency_rates_timestamp";

    private final String requestUrl = "https://v6.exchangerate-api.com/v6/" + SecretConstants.ExchangeRateApiKey + "/latest/USD";

    public Date getDateOfUpdateCurrency(){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TIMESTAMP_KEY))){
            return new Date(Long.parseLong((String) redisTemplate.opsForValue().get(TIMESTAMP_KEY)));
        }
        return new Date();
    }

    public double convert(String fromCurrency, String toCurrency, double amount) {
        HashMap<String, Double> rates = getConversionRates();
        if (!rates.containsKey(fromCurrency) || !rates.containsKey(toCurrency))
            throw new IllegalArgumentException("Неподдерживаемый код");
        double rateFrom = rates.get(fromCurrency);
        double rateTo = rates.get(toCurrency);
        System.out.println("Convert: " + fromCurrency + amount + " to " + toCurrency );
        return amount * (rateTo / rateFrom);

    }

    private HashMap<String, Double> getConversionRates(){
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TIMESTAMP_KEY))){
            if(new Date().getTime() - Long.parseLong((String) redisTemplate.opsForValue().get(TIMESTAMP_KEY)) < 1800000){
                return getRates();
            }
        }
        HashMap<String, Double> rates = requestConversionRates();

        saveRates(rates);
        return rates;
    }

    private void saveRates(HashMap<String, Double> rates) {
        redisTemplate.opsForHash().putAll(RATE_KEY, rates);
        redisTemplate.opsForValue().set(TIMESTAMP_KEY, String.valueOf(new Date().getTime()));
    }

    private HashMap<String, Double> getRates() {
        Map<Object, Object> redisHash = redisTemplate.opsForHash().entries(RATE_KEY);
        HashMap<String, Double> rates = new HashMap<>();

        for (Map.Entry<Object, Object> entry : redisHash.entrySet()) {
            String currency = (String) entry.getKey();
            Double rate;
            if (entry.getValue() instanceof Integer)
                rate = Double.valueOf ((Integer) entry.getValue());
            else rate = (Double) entry.getValue();
            rates.put(currency, rate);
        }
        return rates;
    }

    private HashMap<String, Double> requestConversionRates(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HashMap<String, Double> conversionRates = new HashMap<>();
        try {
            HttpGet request = new HttpGet(requestUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                ObjectMapper mapper = new ObjectMapper();
                JSONObject rates = new JSONObject(EntityUtils.toString(entity));
                Map<Object, Object> result = mapper.readValue(rates.get("conversion_rates").toString(), HashMap.class);
                for (Map.Entry<Object, Object> entry : result.entrySet()) {
                    String currency = (String) entry.getKey();
                    Double rate;
                    if (entry.getValue() instanceof Integer)
                        rate = Double.valueOf ((Integer) entry.getValue());
                    else rate = (Double) entry.getValue();
                    conversionRates.put(currency, rate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return conversionRates;
    }
}
