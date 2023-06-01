package org.chous.taco.controllers;

import org.chous.taco.models.Purchase;
import org.chous.taco.models.Taco;
import org.chous.taco.repositories.PurchaseRepository;
import org.chous.taco.repositories.TacoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class PurchaseController {

    private final TacoRepository tacoRepository;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseController(TacoRepository tacoRepository, PurchaseRepository purchaseRepository) {
        this.tacoRepository = tacoRepository;
        this.purchaseRepository = purchaseRepository;
    }


    @GetMapping("/cart")
    public String cart(Model model, @ModelAttribute("purchase") Purchase purchase) {

        List<Taco> activeTacos = tacoRepository.findTacoByActive(true);
        BigDecimal totalPrise = new BigDecimal("0.0");

        for (Taco taco : activeTacos) {
            totalPrise = totalPrise.add(taco.getPrice());
        }

        model.addAttribute("activeTacos", activeTacos);
        model.addAttribute("totalPrise", totalPrise);

        return "cart";
    }


    @PostMapping("/cart")
    public String createPurchase(@ModelAttribute("purchase") @Valid Purchase purchase, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "cart";
        }

        List<Taco> activeTacos = tacoRepository.findTacoByActive(true);

        for (Taco taco : activeTacos) {
            taco.setActive(false);
            tacoRepository.save(taco);
        }

        purchase.setTacos(activeTacos);

        purchaseRepository.save(purchase);

        return "redirect:/done";
    }


    @GetMapping("/done")
    public String done() {
        return "done";
    }

}
