package org.chous.taco.dao;

import org.chous.taco.models.Ingredient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngredientDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IngredientDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Ingredient> ingredients() {
        return jdbcTemplate.query("SELECT * FROM ingredient", new BeanPropertyRowMapper<>(Ingredient.class));
    }


    public Ingredient show(int id) {
        return jdbcTemplate.query("SELECT * FROM ingredient WHERE id=?", new Object[]{id},
                        new BeanPropertyRowMapper<>(Ingredient.class))
                .stream().findAny().orElse(null);
    }


    public void save(Ingredient ingredient) {
        jdbcTemplate.update("INSERT INTO ingredient (name, category, img_path, weight, calories, protein, fat, carbs, price) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                ingredient.getName(), ingredient.getType(), ingredient.getImgPath(),
                ingredient.getWeight(), ingredient.getCalories(), ingredient.getProtein(),
                ingredient.getFat(), ingredient.getCarbs(), ingredient.getPrice());
    }


    public void update(int id, Ingredient updatedIngredient) {
        jdbcTemplate.update("UPDATE ingredient SET name=?, category=?, img_path=?, weight=?, calories=?, protein=?, fat=?, carbs=?, price=? WHERE id=?",
                updatedIngredient.getName(), updatedIngredient.getType(), updatedIngredient.getImgPath(),
                updatedIngredient.getWeight(), updatedIngredient.getCalories(), updatedIngredient.getProtein(),
                updatedIngredient.getFat(), updatedIngredient.getCarbs(), updatedIngredient.getPrice(), id);
    }


    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM ingredient WHERE id=?", id);
    }

}
