package com.qwerky.microservicepoc.controller;

import com.qwerky.microservicepoc.model.CartItem;
import com.qwerky.microservicepoc.service.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/cart")
public class CartItemController {

    @Autowired
    private CartItemRepository repository;

    @GetMapping("/item/{id}")
    public CartItem getCartItem(@PathVariable String id) {
        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/item")
    public CartItem createCartItem(@RequestBody CartItem item) {
        if (item.getId() == null) {
            return repository.insert(item);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id is not allowed when inserting objects");
        }
    }

    @PutMapping("/item/{id}")
    public CartItem updateCartItem(@PathVariable String id, @RequestBody CartItem item) {
        if (repository.existsById(id)) {
            item.setId(id);
            return repository.save(item);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart Item with id " + id + " does not exist.");
        }
    }

    @DeleteMapping("/item/{id}")
    public void deleteCartItem(@PathVariable String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart Item with id " + id + " does not exist.");
        }
    }
}
