package org.chous.taco.repositories;

import org.chous.taco.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    User findById(int id);
    Optional<User> findByEmail(String email);
    User findByActivationCode(String code);
    User findByResetPasswordToken(String token);
}