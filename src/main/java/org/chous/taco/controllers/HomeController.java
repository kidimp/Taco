package org.chous.taco.controllers;

import org.chous.taco.models.Purchase;
import org.chous.taco.models.Taco;
import org.chous.taco.repositories.TacosRepository;
import org.chous.taco.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final TacosRepository tacoRepository;

    @Autowired
    public HomeController(TacosRepository tacoRepository) {
        this.tacoRepository = tacoRepository;
    }


    @GetMapping("/")
    public String home(Model model, @ModelAttribute("tacoToBeAdded") Taco taco) {

        // Получаем информацию, в какой роли (anonymous, user, admin) пользователь зашёл на страницу.
        // Это нужно для корректного отображения хедера.
        UserService.getCurrentPrincipalUserRole(model);

        // Получаем все стандартные (не кастомные) тако из базы данных.
        List<Taco> standardTacosUnsorted = tacoRepository.findTacoByCustom(false);

        // Большой List<Taco> разбиваем на множество листов с тако, сгрупированных по одинаковым именам.
        Collection<List<Taco>> standardTacos = standardTacosUnsorted.stream()
                .collect(Collectors.groupingBy(Taco::getName)).values();

        // Сортируем каждый из листов с тако по возрастанию размера, чтобы было удобнее выводить на view.
        // Умножили на 10, чтобы double нормально сконвертировался в int.
        for (List<Taco> list : standardTacos) {
            list.sort((left, right) -> (int) (left.getSize() * 10 - right.getSize() * 10));
        }

        model.addAttribute("standardTacos", standardTacos);

        return "home";
    }


    @PostMapping("/")
    public String home(@RequestParam(value="tacoToAdd") Taco tacoToAdd) {

        tacoToAdd.setActive(true);
        tacoRepository.save(tacoToAdd);

        return "redirect:/cart";
    }


    @GetMapping("/page-under-construction")
    public String pageUnderConstruction() {

        return "page-under-construction";
    }

}
