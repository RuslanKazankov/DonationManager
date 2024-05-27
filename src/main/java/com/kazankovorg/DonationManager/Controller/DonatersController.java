package com.kazankovorg.DonationManager.Controller;

import com.kazankovorg.DonationManager.Exceptions.DonaterNotFoundException;
import com.kazankovorg.DonationManager.Models.Donater;
import com.kazankovorg.DonationManager.Models.Donation;
import com.kazankovorg.DonationManager.Models.Note;
import com.kazankovorg.DonationManager.Models.UserEntity;
import com.kazankovorg.DonationManager.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.data.domain.Page;

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
    @Autowired
    private NoteService noteService;

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

    @GetMapping("/donater/{donaterId}")
    public String getDonaterProfile(@PathVariable("donaterId") Long donaterId,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    Model model){
        Donater donater = donaterService.getDonaterById(donaterId);
        if(donater == null)
            throw new DonaterNotFoundException("Donater with id " + donaterId + " not found");

        Page<Donation> donations = donationService.getDonationListByDonaterId(donaterId, page - 1);
        if (donations.getTotalPages() < page && donations.getTotalPages() != 0){
            return "redirect:/donater/".concat(donaterId.toString());
        }
        List<Note> notes = new ArrayList<>();
        for(var donation : donations.getContent()){
            Note note = noteService.getNoteByDonationId(donation.getId());
            notes.add(note);
        }

        model.addAttribute("donations", donations.getContent());
        model.addAttribute("notes", notes);
        model.addAttribute("donater", donater);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageCount", Math.max(donations.getTotalPages(), 1));
        return "donater";
    }

    @ExceptionHandler(DonaterNotFoundException.class)
    public ModelAndView handleDonaterNotFoundException(DonaterNotFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("error-page"); // Имя вашей страницы ошибки
        modelAndView.addObject("errorMessage", ex.getMessage());
        return modelAndView;
    }
}
