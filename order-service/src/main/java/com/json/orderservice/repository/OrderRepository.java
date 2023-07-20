package com.json.orderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.json.orderservice.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{
    
}
