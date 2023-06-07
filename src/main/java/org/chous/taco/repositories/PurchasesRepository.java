package org.chous.taco.repositories;

import org.chous.taco.models.Purchase;
import org.springframework.data.repository.CrudRepository;

public interface PurchasesRepository extends CrudRepository<Purchase, Integer> {

}
