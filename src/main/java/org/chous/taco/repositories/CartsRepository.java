package org.chous.taco.repositories;

import org.chous.taco.models.Cart;
import org.springframework.data.repository.CrudRepository;

public interface CartsRepository extends CrudRepository<Cart, Integer> {

    Cart findCartByUserIdAndActive(int userId, boolean active);

}
