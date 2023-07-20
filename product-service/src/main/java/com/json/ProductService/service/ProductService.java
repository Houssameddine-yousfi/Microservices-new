package com.json.ProductService.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.json.ProductService.dto.ProductRequest;
import com.json.ProductService.dto.ProductResponse;
import com.json.ProductService.model.Product;
import com.json.ProductService.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    

    private final ProductRepository productRepository;


    public void createProduct(ProductRequest productRequest){
        Product product = Product.builder()
            .name(productRequest.getName())
            .description(productRequest.getName())
            .price(productRequest.getPrice())
            .build();

        productRepository.save(product);
        log.info("Product {} is saved", product.getId() );
    }


    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product p){
        return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .price(p.getPrice())
            .build();
    }
}
