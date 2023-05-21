package org.chous.taco.dao;

import org.chous.taco.models.Taco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TacoDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TacoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Taco> tacos() {
        return jdbcTemplate.query("SELECT * FROM taco", new BeanPropertyRowMapper<>(Taco.class));
    }


    public Integer getLastRecordId() {
        try {
            return jdbcTemplate.queryForObject("SELECT id FROM taco WHERE id=(SELECT max(id) FROM taco);",
                    Integer.class);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }


    public List<Taco> getActiveTacos() {
        return jdbcTemplate.query("SELECT * FROM taco WHERE is_active=true", new BeanPropertyRowMapper<>(Taco.class));
    }


    public Taco show(int id) {
        return jdbcTemplate.query("SELECT * FROM taco WHERE id=?",
                        new Object[]{id}, new BeanPropertyRowMapper<>(Taco.class))
                .stream().findAny().orElse(null);
    }


    public void save(Taco taco) {
        jdbcTemplate.update("INSERT INTO taco (name, size, weight, calories, protein, fat, carbs, price, is_custom, is_active) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                taco.getName(), taco.getSize(), taco.getWeight(), taco.getCalories(),
                taco.getProtein(), taco.getFat(), taco.getCarbs(), taco.getPrice(),
                taco.isCustom(), taco.isActive());
    }

    public void saveIngredients(int taco_id, int ingredient_id) {
        jdbcTemplate.update("INSERT INTO taco_ingredients (taco_id, ingredient_id) VALUES (?, ?)",
                taco_id, ingredient_id);
    }


    public void update(int id, Taco updatedTaco) {
        jdbcTemplate.update("UPDATE taco SET name=?, size=?, weight=?, calories=?, protein=?, fat=?, carbs=?, price=?, is_custom=?, is_active=? WHERE id=?",
                updatedTaco.getName(), updatedTaco.getSize(), updatedTaco.getWeight(), updatedTaco.getCalories(),
                updatedTaco.getProtein(), updatedTaco.getFat(), updatedTaco.getCarbs(), updatedTaco.getPrice(),
                updatedTaco.isCustom(), updatedTaco.isActive(), id);
    }


    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM taco WHERE id=?", id);
    }

}
