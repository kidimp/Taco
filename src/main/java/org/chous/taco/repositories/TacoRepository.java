package org.chous.taco.repositories;

import org.chous.taco.models.Taco;
import org.springframework.data.repository.CrudRepository;


public interface TacoRepository extends CrudRepository<Taco, Integer> {

}
