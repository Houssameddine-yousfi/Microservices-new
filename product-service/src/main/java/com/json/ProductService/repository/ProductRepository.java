package com.json.ProductService.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.json.ProductService.model.Product;

public interface ProductRepository extends MongoRepository<Product, String>{
}
