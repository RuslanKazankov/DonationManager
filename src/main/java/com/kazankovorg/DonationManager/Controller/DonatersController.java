package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class DonatersController {

    @Autowired
    private UserService userService;
    @Autowired
    private CurrencyConvertService currencyConvertService;
    @Autowired
    private DonationService donationService;
    @Autowired
    private DonaterService donaterService;

    @ModelAttribute("user")
    public UserEntity sessionUser(){
        return userService.getSessionUser();
    }

    @ModelAttribute("currencies")
    public List<String> currencies(){
        return new ArrayList<>(List.of(
                "EUR","USD","RUB","BYN","KZT","UAH", "BRL", "TRY"
        ));
    }

    @GetMapping("/donaters")
    public String getDonaters(@RequestParam(value = "page", defaultValue = "1") Integer page,
                              @RequestParam(value = "currency", defaultValue = "RUB") String currency,
                              Model model){
        int pagesOfDonaters = donaterService.getPagesOfDonaters(sessionUser().getId());
        if (page < 1 || page > pagesOfDonaters)
            return "redirect:/donaters";
        if (!currencies().contains(currency)){
            model.addAttribute("Error", "Currency не поддерживается или не найден");
            currency = "RUB";
        }
        List<Donater> donaters = donaterService.getTopDonaters(sessionUser().getId(), page - 1);
        List<Double> sumsOfDonationsByDonaters = new ArrayList<>();
        List<Long> countsOfDonationsByDonaters = new ArrayList<>();
        //Запросы в цикле, но цикл имеет константную величину
        for (var donater : donaters){
            double sum = 0;
            HashMap<String, Double> currencies = donationService.getTotalAmountByCurrencyAndDonaterId(donater.getId());
            for (String curr : currencies.keySet()){
                if (!curr.equals(currency)){
                    try {
                        sum += currencyConvertService.convert(curr, currency, currencies.get(curr));
                    }
                    catch (IllegalArgumentException e){
                        System.out.println("Не получилось конвертировать: " + e.getMessage());
                    }
                }else{
                    sum += currencies.get(curr);
                }
            }
            sumsOfDonationsByDonaters.add(sum);
            countsOfDonationsByDonaters.add(donationService.getCountOfDonationsByDonaterId(donater.getId()));
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        model.addAttribute("currencyDate", formatter.format(currencyConvertService.getDateOfUpdateCurrency()));
        model.addAttribute("currentPage", page);
        model.addAttribute("currentCurrency", currency);
        model.addAttribute("pageCount", pagesOfDonaters);
        model.addAttribute("donaters", donaters);
        model.addAttribute("sums", sumsOfDonationsByDonaters);
        model.addAttribute("counts", countsOfDonationsByDonaters);
        return "donaters";
    }

    @PostMapping("/setStatus")
    @ResponseBody
    public ResponseEntity<String> setStatus(@RequestParam String donaterName, @RequestParam String status){
        if(donaterService.setStatusByUserIdAndDonaterUsername(sessionUser().getId(), donaterName, status))
            return new ResponseEntity<>("The status is set", HttpStatus.OK);

        return new ResponseEntity<>("The status is not set", HttpStatus.BAD_REQUEST);
    }
}
