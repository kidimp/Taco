package org.chous.taco.components;

import org.chous.taco.models.User;
import org.chous.taco.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UserService userService;

    @Autowired
    public UserValidator(UserService userService) {
        this.userService = userService;
    }


    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors error) {
        User user = (User) o;

        try {
            userService.loadUserByUsername(user.getEmail());
        } catch (UsernameNotFoundException ignored) {
            return; // все ок, пользователь не найден
        }

        error.rejectValue("email", "", "This user already exists");
    }


    public void checkEquality(String password, String passwordConfirm, Errors error) {
        if (!password.equals(passwordConfirm)) {
            error.rejectValue("password", "", "Passwords do not match");
        }
    }

}