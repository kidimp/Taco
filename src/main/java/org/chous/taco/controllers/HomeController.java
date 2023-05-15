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
import org.springframework.web.bind.support.SessionStatus;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

//@SessionAttributes("taco")
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
    public String home() {
        return "home";
    }


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

        List<Ingredient> ingredientsForCurrentTaco = taco.getIngredients();

        for (Ingredient i : ingredientsForCurrentTaco) {
            weight += i.getWeight();
            calories += i.getCalories();
            protein += i.getProtein();
            fat += i.getFat();
            carbs += i.getCarbs();
            price = price.add(i.getPrice());
        }

        taco.setWeight(weight);
        taco.setCalories(calories);
        taco.setProtein(protein);
        taco.setFat(fat);
        taco.setCarbs(carbs);
        taco.setPrice(price);

        tacoDAO.save(taco);

        return "redirect:/";
    }


    @GetMapping("/cart")
    public String cart(@ModelAttribute("purchase") Purchase purchase) {
        return "cart";
    }


}
