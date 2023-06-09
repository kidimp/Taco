package org.chous.taco.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters long")
    private String username;
    @Size(min = 1, message = "Email must not be empty")
    @Email(message = "Email must be valid")
    private String email;
    @Size(min = 3, message = "Password must be at least three characters long")
    private String password;
    private String role;
    private boolean isActive;
    private String activationCode;
    private String resetPasswordToken;


    @OneToMany()
    @JoinTable(
            name = "user_purchases",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "purchase_id", referencedColumnName = "id")
    )
    private List<Purchase> purchases;

}
