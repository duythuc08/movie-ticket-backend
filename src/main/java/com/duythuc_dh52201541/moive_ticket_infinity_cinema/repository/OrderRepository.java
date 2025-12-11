package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Orders;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {
    Optional<Orders> findByOrderId(Long orderId);

    List<Orders> findAllByOrderStatusAndExpiredTimeBefore(OrderStatus orderStatus, LocalDateTime expiredTimeBefore);
}
