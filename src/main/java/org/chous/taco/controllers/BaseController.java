package org.chous.taco.controllers;

import org.chous.taco.models.Cart;
import org.chous.taco.models.Taco;
import org.chous.taco.models.User;
import org.chous.taco.repositories.CartsRepository;
import org.chous.taco.services.UserService;

import java.util.List;

public class BaseController {

    public CartsRepository cartsRepository;


    public void addAndSafeTacoToCartOfCurrentPrincipalUser(Taco tacoToAdd) {
        // Определяем текущего авторизированного пользователя.
        User currentPrincipalUser = UserService.getCurrentPrincipleUser();

        // Определяем, есть ли у текущего авторизированного пользователя активная (не погашенная) корзина.
        Cart cart = cartsRepository.findCartByUserIdAndActive(currentPrincipalUser.getId(), true);

        // Если активной корзины нет, то создаём новую активную корзину для текущего пользователя.;
        if (cart == null) {
            cart = new Cart();
        }
        List<Taco> cartTacos = cart.getTacos();
        // Присваиваем новой активной карзине текущего пользователя.
        cart.setUserId(currentPrincipalUser.getId());

        // Здесь добавляем тако, которые выбрал пользователь, в список.
        cartTacos.add(tacoToAdd);


        // Список с выбранными пользователем тако добавляем в корзину.
        cart.setTacos(cartTacos);
        // Сохраняем корзину в базу данных.
        cartsRepository.save(cart);
    }

}
