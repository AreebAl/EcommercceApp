package com.ecommeceapp.ecommerceapp.admin.service;

import com.ecommeceapp.ecommerceapp.common.exception.ResourceNotFoundException;
import com.ecommeceapp.ecommerceapp.order.entity.Order;
import com.ecommeceapp.ecommerceapp.order.repo.OrderRepository;
import com.ecommeceapp.ecommerceapp.order.status.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ecommeceapp.ecommerceapp.admin.order.AdminOrderDtos.*;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepo;

    @Transactional(readOnly = true)
    public Page<AdminOrderResponse> list(Pageable pageable) {
        return orderRepo.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminOrderResponse get(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Transactional
    public AdminOrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest req) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // basic status rules (simple, but prevents nonsense)
        OrderStatus current = order.getStatus();
        OrderStatus next = req.status();

        if (current == OrderStatus.CANCELLED) {
            throw new ResourceNotFoundException("Cancelled order cannot be updated");
        }
        if (current == OrderStatus.DELIVERED) {
            throw new ResourceNotFoundException("Delivered order cannot be updated");
        }
        if (current == OrderStatus.PENDING_PAYMENT && next != OrderStatus.CANCELLED && next != OrderStatus.PENDING_PAYMENT) {
            throw new ResourceNotFoundException("Order must be PAID before shipping");
        }
        if (current == OrderStatus.PAID && next == OrderStatus.DELIVERED) {
            throw new ResourceNotFoundException("Must SHIP before DELIVER");
        }

        order.setStatus(next);
        orderRepo.save(order);

        return toResponse(order);
    }

    private AdminOrderResponse toResponse(Order o) {
        var items = o.getItems().stream()
                .map(i -> new AdminOrderItemResponse(
                        i.getProduct().getId(),
                        i.getProductName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                ))
                .toList();

        return new AdminOrderResponse(
                o.getId(),
                o.getUser().getEmail(),
                o.getStatus(),
                o.getSubtotal(),
                o.getTotal(),
                o.getCreatedAt(),
                o.getShippingAddress(),
                items
        );
    }
}
