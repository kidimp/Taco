package org.chous.taco.repositories;

import org.chous.taco.models.Taco;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface TacoRepository extends CrudRepository<Taco, Integer> {
    List<Taco> findTacoByCustom(boolean custom);
    List<Taco> findTacoByActive(boolean active);
}
