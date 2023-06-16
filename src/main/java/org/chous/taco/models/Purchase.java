package org.chous.taco.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
//    @NotBlank(message = "Name must not be empty")
    @Size(min = 2, max = 30, message = "Name must not be empty and must be between 2 and 30 characters")
    private String deliveryName;
    //    @Pattern(regexp="^(\\+\\d{3}( )?)?((\\(\\d{2}\\))|\\d{1,3})[- .]?\\d{7}[- .]?$", message="Must be formatted +375 29 1234567")
    private String deliveryPhoneNumber;
    @NotBlank(message = "Street is required")
    private String deliveryStreet;
    @NotBlank(message = "Building is required")
    private String deliveryBuilding;
    @NotBlank(message = "Building entrance is required")
    private String deliveryEntrance;
    @NotBlank(message = "Floor is required")
    private String deliveryFloor;
    @NotBlank(message = "Apartment is required")
    private String deliveryApartment;
    private String deliveryComment;
    @CreditCardNumber(message = "Not a valid credit card number")
    private String ccNumber;
    @Pattern(regexp = "^(0[1-9]|1[0-2])(/)([2-9]\\d)$", message = "Must be formatted MM/YY")
    private String ccExpiration;
    @Digits(integer = 3, fraction = 0, message = "Invalid CVV")
    private String ccCVV;


    @ManyToMany()
    @JoinTable(
            name = "purchase_tacos",
            joinColumns = @JoinColumn(name = "purchase_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "taco_id", referencedColumnName = "id")
    )
    @Size(min = 1, message = "You must choose at least 1 taco")
    private List<Taco> tacos;

}