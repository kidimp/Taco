package org.chous.taco.controllers;

import org.chous.taco.models.Purchase;
import org.chous.taco.models.Taco;
import org.chous.taco.models.User;
import org.chous.taco.repositories.UsersRepository;
import org.chous.taco.repositories.PurchasesRepository;
import org.chous.taco.repositories.TacosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TacosRepository tacoRepository;
    private final PurchasesRepository purchasesRepository;
    private final UsersRepository usersRepository;
    private List<Taco> currentPurchaseTacos;

    @Autowired
    public HomeController(TacosRepository tacoRepository, PurchasesRepository purchasesRepository, UsersRepository usersRepository) {
        this.tacoRepository = tacoRepository;
        this.purchasesRepository = purchasesRepository;
        this.usersRepository = usersRepository;
        currentPurchaseTacos = new ArrayList<>();
    }


    @GetMapping("/")
    public String home(Model model, @ModelAttribute("tacoToAdd") Taco tacoToAdd, @ModelAttribute("purchase") Purchase purchase) {

        // Получаем все стандартные (не кастомные) тако из базы данных.
        List<Taco> standardTacosUnsorted = tacoRepository.findTacoByCustom(false);

        // Большой List<Taco> разбиваем на множество листов с тако, сгрупированных по одинаковым именам.
        Collection<List<Taco>> standardTacos = standardTacosUnsorted.stream()
                .collect(Collectors.groupingBy(Taco::getName)).values();

        // Сортируем каждый из листов с тако по возрастанию размера, чтобы было удобнее выводить на view.
        // Умножили на 10, чтобы double нормально сконвертировался в int.
        for (List<Taco> list : standardTacos) {
            list.sort((left, right) -> (int) (left.getSize() * 10 - right.getSize() * 10));
        }

        // Передаём на view главной страницы все стандартные тако.
        model.addAttribute("standardTacos", standardTacos);

        return "home";
    }


    @PostMapping("/")
    public String home(@RequestParam(value = "tacoToAdd") Taco tacoToAdd) {

        // Здесь добавляем тако, которые выбрал пользователь, в список.
        currentPurchaseTacos.add(tacoToAdd);

        return "redirect:/cart";
    }


    @GetMapping("/cart")
    public String cart(Model model, @ModelAttribute("purchase") Purchase purchase) {

        // Создаём переменную для суммарной цены всех тако в заказе.
        BigDecimal totalPrise = new BigDecimal("0.0");

        // Подсчитываем суммарную стоимость заказа.
        for (Taco taco : currentPurchaseTacos) {
            totalPrise = totalPrise.add(taco.getPrice());
        }

        // Передаём на view корзины все тако в заказе и общую стоимость заказа.
        model.addAttribute("currentPurchaseTacos", currentPurchaseTacos);
        model.addAttribute("totalPrise", totalPrise);

        return "cart";
    }


    @PostMapping("/cart")
    public String createPurchase(@ModelAttribute("purchase") @Valid Purchase purchase, BindingResult bindingResult) {

        // Валидируем, правильно ли заполнены все необходимые формы и не пустой ли заказ.
        if (bindingResult.hasErrors() || currentPurchaseTacos.isEmpty()) {
            return "cart";
        }

        // Список с выбранными пользователем тако добавляем в заказ.
        purchase.setTacos(currentPurchaseTacos);

        // Закрываем заказ.
        purchase.setActive(false);

        // Сохраняем заказ в базу данных.
        purchasesRepository.save(purchase);

        // Определяем текущего авторизированного пользователя.
        int currentPrincipalUserId = 0;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() != "anonymousUser") {
            currentPrincipalUserId = Objects.requireNonNull(usersRepository.findByUsername(authentication.getName())
                    .orElse(null)).getId();
        }
        User currentPrincipalUser = usersRepository.findById(currentPrincipalUserId);

        // Получаем список всех заказов текущего пользователя.
        List<Purchase> currentUserPurchases = currentPrincipalUser.getPurchases();
        // Добавляем текущий заказ в список всех заказов текущего пользователя.
        currentUserPurchases.add(purchase);
        // Обновлённый список всех заказов текущего пользователя добавляем в объект "текощй пользователь".
        currentPrincipalUser.setPurchases(currentUserPurchases);
        // Сохраняем обновлённые данные о пользователе и его заказах в базу данных.
        usersRepository.save(currentPrincipalUser);

        // Очищаем текущий список тако.
        currentPurchaseTacos = new ArrayList<>();

        return "redirect:/done";
    }


    @GetMapping("/done")
    public String done() {
        return "done";
    }


    @GetMapping("/admin")
    public String admin() {
        return "/admin";
    }


    @GetMapping("/profile")
    public String profile() {
        return "/profile";
    }


    @GetMapping("/stats")
    public String stats() {
        return "/stats";
    }

}
