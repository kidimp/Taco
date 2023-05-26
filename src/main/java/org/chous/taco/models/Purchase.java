package org.chous.taco.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank(message="Delivery name is required")
    private String deliveryName;
    @NotBlank(message="Street is required")
    private String deliveryStreet;
//    @NotBlank(message="City is required")
//    private String deliveryCity;
//    @NotBlank(message="State is required")
//    private String deliveryState;
//    @NotBlank(message="Zip code is required")
//    private String deliveryZip;
//    @CreditCardNumber(message="Not a valid credit card number")
//    private String ccNumber;
//    @Pattern(regexp="^(0[1-9]|1[0-2])(/)([2-9]\\d)$", message="Must be formatted MM/YY")
//    private String ccExpiration;
//    @Digits(integer=3, fraction=0, message="Invalid CVV")
//    private String ccCVV;

    @ManyToMany()
    @JoinTable(
            name = "purchase_tacos",
            joinColumns = @JoinColumn(name = "purchase_id"),
            inverseJoinColumns = @JoinColumn(name = "taco_id")
    )
    @Size(min = 1, message = "You must choose at least 1 ingredient")
    private Set<Taco> tacos;

}
