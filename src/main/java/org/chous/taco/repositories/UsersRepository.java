package org.chous.taco.repositories;

import org.chous.taco.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User findByActivationCode(String code);

    User findByResetPasswordToken(String token);

    @Query(value = "SELECT * FROM user WHERE EXISTS (SELECT userId FROM bets WHERE userId=user.id)", nativeQuery = true)
    List<User> findAllActiveUsers();

}