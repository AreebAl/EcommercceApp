package com.ecommeceapp.ecommerceapp.payment.service;



import com.ecommeceapp.ecommerceapp.common.exception.ResourceNotFoundException;


import com.ecommeceapp.ecommerceapp.order.entity.Order;
import com.ecommeceapp.ecommerceapp.order.repo.OrderRepository;
import com.ecommeceapp.ecommerceapp.order.status.OrderStatus;
import com.ecommeceapp.ecommerceapp.payment.dto.PaymentDtos;
import com.ecommeceapp.ecommerceapp.payment.entity.Payment;
import com.ecommeceapp.ecommerceapp.payment.repo.PaymentRepository;
import com.ecommeceapp.ecommerceapp.payment.status.PaymentMethod;
import com.ecommeceapp.ecommerceapp.payment.status.PaymentStatus;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import com.ecommeceapp.ecommerceapp.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    private User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public PaymentDtos.PaymentResponse create(String email, PaymentDtos.CreatePaymentRequest req) {
        User user = getUserByEmail(email);

        Order order = orderRepo.findById(req.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + req.orderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Not allowed to pay for this order");
        }

        paymentRepo.findByOrderId(order.getId()).ifPresent(p -> {
            throw new ResourceNotFoundException("Payment already exists for this order");
        });

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResourceNotFoundException("Cannot pay for cancelled order");
        }

        Payment payment = Payment.builder()
                .order(order)
                .method(req.method())
                .status(PaymentStatus.INITIATED)
                .amount(order.getTotal())
                .build();

        payment = paymentRepo.save(payment);

        // COD can auto-success (optional)
        if (req.method() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment = paymentRepo.save(payment);
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
        }

        return toResponse(payment);
    }

    @Transactional
    public PaymentDtos.PaymentResponse mark(String email, Long orderId, PaymentDtos.MarkPaymentRequest req) {
        User user = getUserByEmail(email);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        // idempotency: if already success, don’t change
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return toResponse(payment);
        }

        payment.setStatus(req.status());
        payment.setProviderRef(req.providerRef());
        payment.setFailureReason(req.failureReason());

        payment = paymentRepo.save(payment);

        if (req.status() == PaymentStatus.SUCCESS) {
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
        } else if (req.status() == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            orderRepo.save(order);
        }

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDtos.PaymentResponse getByOrder(String email, Long orderId) {
        User user = getUserByEmail(email);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not allowed");
        }

        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));

        return toResponse(payment);
    }

    private PaymentDtos.PaymentResponse toResponse(Payment p) {
        return new PaymentDtos.PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getMethod(),
                p.getStatus(),
                p.getAmount(),
                p.getProviderRef(),
                p.getFailureReason(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
