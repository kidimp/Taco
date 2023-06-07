package org.chous.taco.controllers;

import org.chous.taco.models.User;
import org.chous.taco.services.RegistrationService;
import org.chous.taco.components.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;


@Controller
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserValidator userValidator;

    @Autowired
    public RegistrationController(RegistrationService registrationService, UserValidator userValidator) {
        this.registrationService = registrationService;
        this.userValidator = userValidator;
    }


    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }


    @GetMapping("/registration")
    public String registration(@ModelAttribute("user") User user) {
        return "auth/registration";
    }


    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                          String password, String passwordConfirm) {

        userValidator.validate(user, bindingResult);
        userValidator.checkEquality(password, passwordConfirm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "/auth/registration";
        }

        registrationService.register(user);

        return "auth/activation-message";
    }


    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = registrationService.activateUser(code);

        if (isActivated) {
            model.addAttribute("message", "User successfully activated!");
            System.out.println("User is activated!");
        } else {
            model.addAttribute("message", "Activation code is not found!");
            System.out.println("Activation code is not found!");
        }

        return "auth/login";
    }


    @GetMapping("/reset-password")
    public String showEmailFormForResetPassword() {
        return "auth/reset-password";
    }


    @PostMapping("/reset-password")
    public String processEmailFormForResetPassword(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "/auth/reset-password";
        }

        String email = user.getEmail();

        registrationService.updateResetPasswordToken(email);

        return "auth/reset-message";
    }


    @GetMapping("/reset-form/{token}")
    public String showResetPasswordForm(Model model, @PathVariable String token) {
        model.addAttribute("token", token);
        return "auth/reset-form";
    }


    @PostMapping("/reset-form/{token}")
    public String processResetPasswordForm(Model model, @PathVariable String token,
                                           String password, String passwordConfirm) {

        User user = registrationService.getByResetPasswordToken(token);
        model.addAttribute("user", user);

        if (!password.equals(passwordConfirm)) {
            return "auth/reset-form";
        }

        registrationService.updatePassword(user, password);

        model.addAttribute("message", "You have successfully changed your password.");

        return "auth/reset-form";
    }


}
