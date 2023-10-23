package org.chous.taco.controllers;

import org.chous.taco.models.*;
import org.chous.taco.repositories.CartsRepository;
import org.chous.taco.repositories.UsersRepository;
import org.chous.taco.repositories.TacosRepository;
import org.chous.taco.services.MailService;
import org.chous.taco.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController extends BaseController {

    private final TacosRepository tacoRepository;
    private final UsersRepository usersRepository;
    private final MailService mailService;

    @Autowired
    public HomeController(TacosRepository tacoRepository, CartsRepository cartsRepository, UsersRepository usersRepository, MailService mailService) {
        this.tacoRepository = tacoRepository;
        this.mailService = mailService;
        this.cartsRepository = cartsRepository;
        this.usersRepository = usersRepository;
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

        // Определяем, какой пользователь пытается добавить тако в корзину.
        // Если это аноним, то отправляем его на страницу логина.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() == "anonymousUser") {
            return "redirect:/login";
        } else {
            addAndSafeTacoToCartOfCurrentPrincipalUser(tacoToAdd);
        }

        return "redirect:/cart";
    }


    @GetMapping("/cart")
    public String cart(Model model, @ModelAttribute("purchase") Purchase purchase) {

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        List<Taco> cartTacos = defineCurrentPrincipalUserCart(currentPrincipalUser);

        // Создаём переменную для суммарной цены всех тако в заказе.
        BigDecimal totalPrise = new BigDecimal("0.0");

        // Подсчитываем суммарную стоимость заказа.
        if (cartTacos != null) {
            for (Taco taco : cartTacos) {
                totalPrise = totalPrise.add(taco.getPrice());
            }
        }

//        // Получаем список сохранённых адресов доставки пользователя.
//        List<DeliveryAddress> deliveryAddresses = deliveryAddressesRepository.findAllByUserId(currentPrincipalUser.getId());
//
//        // Если у пользователя есть сохранённые адреса доставки, то добавляем их в модель, чтобы отобразить их на странице.
//        if (!deliveryAddresses.isEmpty()) {
//            model.addAttribute("deliveryAddresses", deliveryAddresses);
//        }

        // Передаём на view корзины все тако в заказе, общую стоимость заказа и текущего пользователя.
        model.addAttribute("cartTacos", cartTacos);
        model.addAttribute("totalPrise", totalPrise);
        model.addAttribute("currentPrincipalUser", currentPrincipalUser);

        return "cart";
    }


    @PostMapping("/cart")
    public String createPurchase(@ModelAttribute("purchase") @Valid Purchase purchase, BindingResult bindingResult) {

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        List<Taco> cartTacos = defineCurrentPrincipalUserCart(currentPrincipalUser);

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        Cart cart = cartsRepository.findCartByUserIdAndActive(currentPrincipalUser.getId(), true);

        // Валидируем, правильно ли заполнены все необходимые формы и не пустой ли заказ.
        if (bindingResult.hasErrors() || cartTacos.isEmpty()) {
            return "redirect:/cart";
        }

        // Делаем корзину неактивной (погашаем корзину).
        cart.setActive(false);
        // Сохраняем корзину в базу данных.
        cartsRepository.save(cart);

        // Присваиваем активному заказу текущего пользователя.
        purchase.setUserId(currentPrincipalUser.getId());
        // Список с выбранными пользователем тако добавляем в заказ.
        purchase.setTacos(cartTacos);
        // Получаем список всех заказов текущего пользователя.
        List<Purchase> currentUserPurchases = currentPrincipalUser.getPurchases();
        // Добавляем текущий заказ в список всех заказов текущего пользователя.
        currentUserPurchases.add(purchase);
        // Обновлённый список всех заказов текущего пользователя добавляем в объект "текощй пользователь".
        currentPrincipalUser.setPurchases(currentUserPurchases);
        // Сохраняем обновлённые данные о пользователе и его заказах в базу данных.
        usersRepository.save(currentPrincipalUser);

        activeCartMessage(currentPrincipalUser, cart);

        // Логика корзины и покупки разделена, чтобы мы могли правильно отображать и проверять поля в заказе,
        // пока пользователь заполняет корзину.

        return "redirect:/done";
    }


    @PostMapping("/deleteTaco")
    public String deleteTacoFromCart(@ModelAttribute("taco") Taco taco, @RequestParam(value = "tacoToDelete") Taco tacoToDelete) {

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        List<Taco> cartTacos = defineCurrentPrincipalUserCart(currentPrincipalUser);

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        Cart cart = cartsRepository.findCartByUserIdAndActive(currentPrincipalUser.getId(), true);

        cartTacos.remove(tacoToDelete);

        // Сохраняем корзину в базу данных.
        cartsRepository.save(cart);

        return "redirect:/cart";
    }


    @GetMapping("/done")
    public String done() {
        return "done";
    }


    @GetMapping("/admin")
    public String admin() {
        return "/admin";
    }


    @GetMapping("/stats")
    public String stats() {
        return "/stats";
    }

    private List<Taco> defineCurrentPrincipalUserCart(User currentPrincipalUser) {

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        Cart cart = cartsRepository.findCartByUserIdAndActive(currentPrincipalUser.getId(), true);

        // Если активная корзина есть, то берём из неё все тако, чтобы отобразить их на странице.
        // Если активной корзины нет, то создаём новый пустой список тако.
        if (cart != null) {
            return cart.getTacos();
        } else {
            return new ArrayList<>();
        }
    }


    public void activeCartMessage(User user, Cart cart) {
        List<Taco> tacos = cart.getTacos();
        StringBuilder tacosInfoRaw = new StringBuilder();
        for (Taco taco : tacos) {
            tacosInfoRaw.append(taco.getName()).append(" ").append(taco.getPrice()).append(" byn").append(",\n");
        }
        String tacosInfo = tacosInfoRaw.substring(0, tacosInfoRaw.length() - 2); // Два знака, потому что коретка

        if (!StringUtils.isEmpty(user.getEmail())) {
            String subject = "We already wrapping your taco";
            String text = String.format(
                    "Hello, %s! \n" +
                            "Thank you for your order!\n" +
                            "You ordered: \n" + tacosInfo,
                    user.getUsername()
            );

            mailService.send(user.getEmail(), subject, text);
        }
    }

}