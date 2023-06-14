package org.chous.taco.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DeliveryAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
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
}
