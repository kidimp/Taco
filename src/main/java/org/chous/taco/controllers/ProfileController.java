package org.chous.taco.controllers;

import org.chous.taco.models.DeliveryAddress;
import org.chous.taco.models.User;
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

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProfileController {

    private final DeliveryAddressesRepository deliveryAddressesRepository;

    @Autowired
    public ProfileController(DeliveryAddressesRepository deliveryAddressesRepository) {
        this.deliveryAddressesRepository = deliveryAddressesRepository;
    }


    @GetMapping("/profile")
    public String profile(Model model) {
        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Добавляем в модель имя текущего пользователя.
        model.addAttribute("username", currentPrincipalUser.getUsername());

        // Получаем список сохранённых адресов доставки пользователя.
        List<DeliveryAddress> deliveryAddresses = deliveryAddressesRepository.findAllByUserId(currentPrincipalUser.getId());

        for (DeliveryAddress address : deliveryAddresses) {
            System.out.println(address.getDeliveryStreet());
        }

        // Если у пользователя есть сохранённые адреса доставки, то добавляем их в модель, чтобы отобразить их на странице.
        if (!deliveryAddresses.isEmpty()) {
            model.addAttribute("deliveryAddresses", deliveryAddresses);
        }

        return "/profile";
    }


    @GetMapping("profile/new_delivery_address")
    public String newDeliveryAddress(@ModelAttribute("deliveryAddress") DeliveryAddress deliveryAddress) {
        System.out.println();
        return "profile/new_delivery_address";
    }


    @PostMapping("/profile")
    public String createNewDeliveryAddress(@ModelAttribute("deliveryAddress") @Valid DeliveryAddress deliveryAddress, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/profile/new_delivery_address";
        }

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        deliveryAddress.setUserId(currentPrincipalUser.getId());
        deliveryAddressesRepository.save(deliveryAddress);

        // Получаем список сохранённых адресов доставки пользователя.
        List<DeliveryAddress> deliveryAddresses = deliveryAddressesRepository.findAllByUserId(currentPrincipalUser.getId());

        // Если у пользователя нет сохранённых адресов доставки, то создаём новый список.
        if (deliveryAddresses.isEmpty()) {
            deliveryAddresses = new ArrayList<>();
        }
//        // Добавляем новый адрес в список адресов доставки.
//        deliveryAddresses.add(deliveryAddress);
//        // Сохраняем для текущего юзера все его адреса доставки.
//        currentPrincipalUser.setDeliveryAddress(deliveryAddresses);

        return "redirect:/profile";
    }

}
