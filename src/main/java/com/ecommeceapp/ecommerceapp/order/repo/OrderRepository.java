package com.ecommeceapp.ecommerceapp.order.repo;


import com.ecommeceapp.ecommerceapp.order.entity.Order;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
}
