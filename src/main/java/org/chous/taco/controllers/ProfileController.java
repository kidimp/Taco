package org.chous.taco.controllers;

import org.chous.taco.models.CreditCard;
import org.chous.taco.models.DeliveryAddress;
import org.chous.taco.models.User;
import org.chous.taco.repositories.CreditCardsRepository;
import org.chous.taco.repositories.DeliveryAddressesRepository;
import org.chous.taco.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;

@Controller
public class ProfileController {

    private final DeliveryAddressesRepository deliveryAddressesRepository;
    private final CreditCardsRepository creditCardsRepository;

    @Autowired
    public ProfileController(DeliveryAddressesRepository deliveryAddressesRepository, CreditCardsRepository creditCardsRepository) {
        this.deliveryAddressesRepository = deliveryAddressesRepository;
        this.creditCardsRepository = creditCardsRepository;
    }


    @GetMapping("/profile")
    public String profile(Model model) {
        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Добавляем в модель имя текущего пользователя.
        model.addAttribute("currentPrincipalUser", currentPrincipalUser);

        // Получаем список сохранённых адресов доставки пользователя.
        List<DeliveryAddress> deliveryAddresses = deliveryAddressesRepository.findAllByUserId(currentPrincipalUser.getId());

        // Если у пользователя есть сохранённые адреса доставки, то добавляем их в модель, чтобы отобразить их на странице.
        if (!deliveryAddresses.isEmpty()) {
            model.addAttribute("deliveryAddresses", deliveryAddresses);
        }

        // Получаем список сохранённых кредитных карт пользователя.
        List<CreditCard> creditCards = creditCardsRepository.findAllByUserId(currentPrincipalUser.getId());

        // Если у пользователя есть сохранённые кредитные карты, то добавляем их в модель, чтобы отобразить их на странице.
        if (!creditCards.isEmpty()) {
            model.addAttribute("creditCards", creditCards);
        }

        return "/profile";
    }

    @Transactional // Эта аннотация позволяет совершать удаление из базы данных
    @PostMapping("/profile")
    public String profile(@RequestParam(required = false, value = "deliveryAddressToDelete") DeliveryAddress deliveryAddressToDelete,
                          @RequestParam(required = false, value = "creditCardToDelete") CreditCard creditCardToDelete) {

        if (deliveryAddressToDelete != null) {
            deliveryAddressesRepository.deleteDeliveryAddressById(deliveryAddressToDelete.getId());
        }

        if (creditCardToDelete != null) {
            creditCardsRepository.deleteCreditCardById(creditCardToDelete.getId());
        }

        return "redirect:/profile";
    }


    @GetMapping("profile/new_delivery_address")
    public String newDeliveryAddress(@ModelAttribute("deliveryAddress") DeliveryAddress deliveryAddress) {

        return "profile/new_delivery_address";
    }


    @PostMapping("profile/new_delivery_address")
    public String createNewDeliveryAddress(@ModelAttribute("deliveryAddress") @Valid DeliveryAddress deliveryAddress, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/profile/new_delivery_address";
        }

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        deliveryAddress.setUserId(currentPrincipalUser.getId());
        deliveryAddressesRepository.save(deliveryAddress);

        return "redirect:/profile";
    }


    @GetMapping("profile/new_credit_card")
    public String newCreditCard(@ModelAttribute("creditCard") CreditCard creditCard) {

        return "profile/new_credit_card";
    }


    @PostMapping("profile/new_credit_card")
    public String createCreditCard(@ModelAttribute("creditCard") @Valid CreditCard creditCard, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/profile/new_credit_card";
        }

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        creditCard.setUserId(currentPrincipalUser.getId());

        // Переделаем номер карты, закрыв первые 12 символов звёздочками, и разобъём цифры на группы по четыре штуки.
        String ccNumber = creditCard.getCcNumber().trim();
        String ccNumberWithAsterisk = "**** **** **** " + ccNumber.substring(ccNumber.length() - 4);
        creditCard.setCcNumberWithAsterisk(ccNumberWithAsterisk);

        creditCardsRepository.save(creditCard);

        return "redirect:/profile";
    }


}