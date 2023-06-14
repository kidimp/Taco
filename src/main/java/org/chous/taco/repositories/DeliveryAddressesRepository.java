package org.chous.taco.repositories;

import org.chous.taco.models.DeliveryAddress;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DeliveryAddressesRepository extends CrudRepository<DeliveryAddress, Integer> {
    List<DeliveryAddress> findAllByUserId(int userId);
}
