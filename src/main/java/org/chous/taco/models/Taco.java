package org.chous.taco.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Entity
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
//    @Column(nullable = false, columnDefinition = "TINYINT(1)", name = "is_custom")
    private boolean isCustom;
//    @Column(nullable = false, columnDefinition = "TINYINT(1)", name = "is_active")
    private boolean isActive;

    @ManyToMany()
    @JoinTable(
            name = "taco_ingredients",
            joinColumns = @JoinColumn(name = "taco_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    @Size(min = 1, message = "You must choose at least 1 ingredient")
    private Set<Ingredient> ingredients;


    public Taco() {
    }
}
