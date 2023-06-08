package org.chous.taco.services;

import org.chous.taco.models.User;
import org.chous.taco.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UsersRepository usersRepository;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(UsersRepository usersRepository, MailService mailService, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public void register(User user) {
        user.setEmail(user.getEmail());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setUsername(user.getUsername());

        user.setActivationCode(UUID.randomUUID().toString());

        if (!StringUtils.isEmpty(user.getEmail())) {
            String subject = "Please activate your account";
            String text = String.format(
                    "Hello, %s! \n" +
                            "Welcome to Taco. Please, visit next link to activate your account: http://localhost:8080/activate/%s",
                    user.getUsername(), user.getActivationCode()
            );

            mailService.send(user.getEmail(), subject, text);
        }

        usersRepository.save(user);
    }


    public boolean activateUser(String code) {
        User user = usersRepository.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActive(true);
        user.setActivationCode(null);

        usersRepository.save(user);

        return true;
    }


    @Transactional
    public void updateResetPasswordToken(String email) {
        Optional<User> user = usersRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        user.get().setResetPasswordToken(UUID.randomUUID().toString());

        if (!StringUtils.isEmpty(user.get().getEmail())) {
            String subject = "Please set a new password for your account";
            String text = String.format(
                    "Hello, %s! \n" +
                            "Please visit next link to set a new password for your account: http://localhost:8080/reset-form/%s",
                    user.get().getUsername(), user.get().getResetPasswordToken()
            );

            mailService.send(user.get().getEmail(), subject, text);
        }

        usersRepository.save(user.get());
    }


    public User getByResetPasswordToken(String token) {
        return usersRepository.findByResetPasswordToken(token);
    }


    public void updatePassword(User user, String newPassword) {

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        user.setPassword(newPassword);
        user.setResetPasswordToken(null);

        usersRepository.save(user);
    }


}