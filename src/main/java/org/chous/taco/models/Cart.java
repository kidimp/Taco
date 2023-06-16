package org.chous.taco.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int userId;
    private boolean active;


    {
        active = true;
        tacos = new ArrayList<>();
    }


    @ManyToMany()
    @JoinTable(
            name = "cart_tacos",
            joinColumns = @JoinColumn(name = "cart_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "taco_id", referencedColumnName = "id")
    )
    @Size(min = 1, message = "You must choose at least 1 taco")
    private List<Taco> tacos;

}