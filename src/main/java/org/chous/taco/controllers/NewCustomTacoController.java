package org.chous.taco.controllers;

import org.chous.taco.models.Cart;
import org.chous.taco.models.Ingredient;
import org.chous.taco.models.Taco;
import org.chous.taco.models.User;
import org.chous.taco.repositories.CartsRepository;
import org.chous.taco.repositories.IngredientsRepository;
import org.chous.taco.repositories.TacosRepository;
import org.chous.taco.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class NewCustomTacoController {

    private final TacosRepository tacosRepository;
    private final IngredientsRepository ingredientRepository;
    private final CartsRepository cartsRepository;
    private final UsersRepository usersRepository;

    private int weight = 0;
    private int calories = 0;
    private int protein = 0;
    private int fat = 0;
    private int carbs = 0;
    private BigDecimal price = new BigDecimal("0.0");


    @Autowired
    public NewCustomTacoController(TacosRepository tacoRepository, IngredientsRepository ingredientRepository, CartsRepository cartsRepository, UsersRepository usersRepository) {
        this.tacosRepository = tacoRepository;
        this.ingredientRepository = ingredientRepository;
        this.cartsRepository = cartsRepository;
        this.usersRepository = usersRepository;
    }


    // Передаём на view списки ингредиентов, распределённые по типам (Type).
    @ModelAttribute
    public void addIngredientsToModel(Model model) {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Ingredient.Type[] types = Ingredient.Type.values();
        for (Ingredient.Type type : types) {
            model.addAttribute(type.toString().toLowerCase(), filterByType(ingredients, type));
        }
    }

    private Iterable<Ingredient> filterByType(List<Ingredient> ingredients, Ingredient.Type type) {
        return ingredients
                .stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }


    @GetMapping("/new_custom_taco")
    public String newCustomTaco(@ModelAttribute("newCustomTaco") Taco taco) {
        return "new_custom_taco";
    }

    @PostMapping("/new_custom_taco")
    public String createCustomTaco(@ModelAttribute("newCustomTaco") @Valid Taco taco, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "new_custom_taco";
        }

        // Из view в моделе Taco taco (@ModelAttribute("newCustomTaco")) пришла информация о всех ингридеентах
        // текущего кастомного тако. Сохраняем эти ингридиенты в список и при помощи сеттера записываем этот список в
        // объект тако.
        List<Ingredient> ingredientsForCurrentTaco = taco.getIngredients();
        taco.setIngredients(ingredientsForCurrentTaco);

        // Пробегаемся по списку ингредиентов для текущего кастомного тако и узнаём суммарные значения нужных нам полей.
        for (Ingredient i : ingredientsForCurrentTaco) {
            weight += i.getWeight();
            calories += i.getCalories();
            protein += i.getProtein();
            fat += i.getFat();
            carbs += i.getCarbs();
            price = price.add(i.getPrice());
        }

        double sizeMultiplier = taco.getSize();

        // Значения полей умнажаем на размерный коэффициент и сохраняем значения в объект тако.
        taco.setWeight((int) (weight * sizeMultiplier));
        taco.setCalories((int) (calories * sizeMultiplier));
        taco.setProtein((int) (protein * sizeMultiplier));
        taco.setFat((int) (fat * sizeMultiplier));
        taco.setCarbs((int) (carbs * sizeMultiplier));
        taco.setPrice(price.multiply(BigDecimal.valueOf(sizeMultiplier)));

        // Помечаем, что мы создаём кастомный тако.
        taco.setCustom(true);

        // Сохраняем заполненый объект тако в базу данных. Объект сохранился последним в таблице и после сохранения
        // автоматически получил свой уникальный id.
        tacosRepository.save(taco);

        /** FIX IT Дублируется код в контроллерах HomeController и NewCustomTacoController */

        List<Taco> cartTacos = new ArrayList<>();

        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = getCurrentPrincipleUser();

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        Cart cart = cartsRepository.findCartByUserIdAndActive(currentPrincipalUser.getId(), true);

        // Если активная корзина есть, то берём из неё все тако, чтобы отобразить их на странице.
        // Если активной корзины нет, то создаём новую активную корзину для текущего пользователя.
        if (cart != null) {
            cartTacos = cart.getTacos();
        } else {
            cart = new Cart();
            // Присваиваем новой активной карзине текущего пользователя.
            cart.setUserId(currentPrincipalUser.getId());
        }

        // Определяем, какой пользователь пытается добавить тако в корзину.
        // Если это аноним, то отправляем его на страницу логина.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() != "anonymousUser") {
            // Здесь добавляем тако, которые выбрал пользователь, в список.
            cartTacos.add(taco);
        } else {
            return "redirect:/login";
        }

        // Список с выбранными пользователем тако добавляем в корзину.
        cart.setTacos(cartTacos);
        // Сохраняем корзину в базу данных.
        cartsRepository.save(cart);

        /** FIX IT Дублируется код в контроллерах HomeController и NewCustomTacoController */

        // Очищаем значения полей тако, чтобы можно было создать ещё одно тако с нуля.
        clean();

        return "redirect:/cart";
    }


    private void clean() {
        weight = 0;
        calories = 0;
        protein = 0;
        fat = 0;
        carbs = 0;
        price = new BigDecimal("0.0");
    }


    // Определяем текущего авторизированного пользователя.
    public User getCurrentPrincipleUser() {
        int currentPrincipalUserId = 0;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() != "anonymousUser") {
            currentPrincipalUserId = Objects.requireNonNull(usersRepository.findByUsername(authentication.getName())
                    .orElse(null)).getId();
        }
        return usersRepository.findById(currentPrincipalUserId);
    }

}
