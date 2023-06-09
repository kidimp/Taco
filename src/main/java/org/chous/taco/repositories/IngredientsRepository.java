package org.chous.taco.repositories;

import org.chous.taco.models.Ingredient;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface IngredientsRepository extends CrudRepository<Ingredient, Integer> {
    List<Ingredient> findAll();
}
