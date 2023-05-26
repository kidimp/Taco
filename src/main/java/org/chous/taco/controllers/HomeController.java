package org.chous.taco.controllers;

import org.chous.taco.dao.IngredientDAO;
import org.chous.taco.dao.TacoDAO;
import org.chous.taco.models.Ingredient;
import org.chous.taco.models.Purchase;
import org.chous.taco.models.Taco;
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
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final IngredientDAO ingredientDAO;
    private final TacoDAO tacoDAO;

    private int weight = 0;
    private int calories = 0;
    private int protein = 0;
    private int fat = 0;
    private int carbs = 0;
    private BigDecimal price = new BigDecimal("0.0");


    @Autowired
    public HomeController(IngredientDAO ingredientDAO, TacoDAO tacoDAO) {
        this.ingredientDAO = ingredientDAO;
        this.tacoDAO = tacoDAO;
    }


    @GetMapping("/")
    public String home(Model model) {
        List<Taco> standardTacos = tacoDAO.tacos();

        /** FIX it!
         for (Taco taco : standardTacos) {
         List<Integer> ingredsIds = tacoDAO.getIngredientsId(taco.getId());
         System.out.println("hello");

         taco.setIngredients();
         }
         **/

        model.addAttribute("standardTacos", standardTacos);

        return "home";
    }


    // Передаём на view списки ингредиентов, распределённые по типам (Type).
    @ModelAttribute
    public void addIngredientsToModel(Model model) {
        List<Ingredient> ingredients = ingredientDAO.ingredients();
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
        Set<Ingredient> ingredientsForCurrentTaco = taco.getIngredients();
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
        tacoDAO.save(taco);

        // Находим id текущего кастомного тако (на данный момент последний в соответствующей теблице базы данных).
        int taco_id = tacoDAO.getLastRecordId();

        // Ещё раз пробегаемся по всем ингредиентам текущего кастомного тако и записываем каждый из них
        // в таблицу базы данных, в которой для каждого тако хранятся все его ингредиенты.
        for (Ingredient i : ingredientsForCurrentTaco) {
            tacoDAO.saveIngredients(taco_id, i.getId());
        }

        // Очищаем значения полей.
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

        List<Taco> activeTacos = tacoDAO.getActiveTacos();
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

        List<Taco> activeTacos = tacoDAO.getActiveTacos();
        for (Taco taco : activeTacos) {
            taco.setActive(false);
            tacoDAO.update(taco.getId(), taco);
        }

        return "redirect:/done";
    }


    @GetMapping("/done")
    public String done() {
        return "done";
    }


}
