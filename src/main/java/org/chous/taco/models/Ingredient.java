package org.chous.taco.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "Name must not be empty")
    @Size(min = 2, max = 30, message = "Name must be between 2 and 30 characters")
    private String name;
    private Type type;
    private String imgPath;
    @NotEmpty(message = "Weight must not be empty")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private int weight;
    @NotEmpty(message = "Caloric value must not be empty")
    @Min(value = 1, message = "Caloric value should be at least 1 kcal")
    private int calories;
    @NotEmpty(message = "Weight must not be empty")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private int protein;
    @NotEmpty(message = "Weight must not be empty")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private int fat;
    @NotEmpty(message = "Weight must not be empty")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private int carbs;
    @Min(value = 0, message = "Price must not be negative")
    private BigDecimal price = new BigDecimal("0.0");


    public enum Type {
        WRAP,
        PROTEIN,
        VEGGIES,
        CHEESE,
        SAUCE
    }

}
