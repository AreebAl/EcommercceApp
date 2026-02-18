package com.ecommeceapp.ecommerceapp.payment.repo;


import com.ecommeceapp.ecommerceapp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
