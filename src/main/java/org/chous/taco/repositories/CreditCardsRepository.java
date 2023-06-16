package org.chous.taco.repositories;

import org.chous.taco.models.CreditCard;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CreditCardsRepository extends CrudRepository<CreditCard, Integer> {
    List<CreditCard> findAllByUserId(int userId);
    void deleteCreditCardById(int id);
}
