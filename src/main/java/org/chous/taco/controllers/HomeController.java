package org.chous.taco.controllers;

import org.chous.taco.models.Ingredient;
import org.chous.taco.models.Purchase;
import org.chous.taco.models.Taco;
import org.chous.taco.repositories.IngredientRepository;
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
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TacoRepository tacoRepository;
    private final IngredientRepository ingredientRepository;

    private int weight = 0;
    private int calories = 0;
    private int protein = 0;
    private int fat = 0;
    private int carbs = 0;
    private BigDecimal price = new BigDecimal("0.0");


    @Autowired
    public HomeController(TacoRepository tacoRepository, IngredientRepository ingredientRepository) {
        this.tacoRepository = tacoRepository;
        this.ingredientRepository = ingredientRepository;
    }


    @GetMapping("/")
    public String home(Model model) {
        List<Taco> standardTacos = tacoRepository.findTacoByCustom(false);

        model.addAttribute("standardTacos", standardTacos);

        return "home";
    }

/** FIX IT
 *
 * Сделать переход в корзину и добавление тако в заказ при нажатии на главной странице кнопки "В корзину"
 *
    @PostMapping("/")
    public String home(@ModelAttribute("newStandardTaco") @Valid Taco taco, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "redirect:/home";
        }

        return "redirect:/cart";
    }
**/


    // Передаём на view списки ингредиентов, распределённые по типам (Type).
    @ModelAttribute
    public void addIngredientsToModel(Model model) {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Ingredient.Type[] types = Ingredient.Type.values();
        for (Ingredient.Type type : types) {
            model.addAttribute(type.toString().toLowerCase(), filterByType(ingredients, type));
        }
    }

    private Iterable<Ingredient> filterByType(
            List<Ingredient> ingredients, Ingredient.Type type) {
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

        // Значенія полей умнажаем на размерный коэффициент и сохраняем значения в объект тако.
        taco.setWeight((int) (weight * sizeMultiplier));
        taco.setCalories((int) (calories * sizeMultiplier));
        taco.setProtein((int) (protein * sizeMultiplier));
        taco.setFat((int) (fat * sizeMultiplier));
        taco.setCarbs((int) (carbs * sizeMultiplier));
        taco.setPrice(price.multiply(BigDecimal.valueOf(sizeMultiplier)));

        // Помечаем, что мы создаём кастомный тако.
        taco.setCustom(true);
        // Помечаем, что данный тако принадлежит активному заказу.
        taco.setActive(true);

        // Сохраняем заполненый объект тако в базу данных. Объект сохранился последним в таблице и после сохранения
        // автоматически получил свой уникальный id.
        tacoRepository.save(taco);

        // Очищаем значения полей, чтобы можно было создать еўё одно тако с нуля.
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

        return "redirect:/done";
    }


    @GetMapping("/done")
    public String done() {
        return "done";
    }

}
