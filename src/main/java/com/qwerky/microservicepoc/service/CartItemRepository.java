package com.qwerky.microservicepoc.service;

import com.qwerky.microservicepoc.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
}
