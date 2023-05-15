package org.chous.taco.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Data
public class Taco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "Name must not be empty")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    private String name;
    private double size;
    private int weight;
    private int calories;
    private int protein;
    private int fat;
    private int carbs;
    private BigDecimal price = new BigDecimal("0.0");

    @ManyToMany(targetEntity = Ingredient.class)
//    @JoinTable(
//            name = "taco_ingredient",
//            joinColumns = @JoinColumn(name = "taco_id"),
//            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
//    )
    @Size(min = 1, message = "You must choose at least 1 ingredient")
    private List<Ingredient> ingredients = new ArrayList<>();


    public Taco() {
    }


}
