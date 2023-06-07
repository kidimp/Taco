package org.chous.taco.components;

import org.chous.taco.models.Ingredient;
import org.chous.taco.repositories.IngredientsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IngredientByIdConverter implements Converter<Integer, Ingredient>  {

    private final IngredientsRepository ingredientRepo;

    @Autowired
    public IngredientByIdConverter(IngredientsRepository ingredientRepo) {
        this.ingredientRepo = ingredientRepo;
    }


    @Override
    public Ingredient convert(Integer id) {
        return ingredientRepo.findById(id).orElse(null);
    }
}
