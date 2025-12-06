package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.OrderTickets;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderTicketRepository extends JpaRepository<OrderTickets,String> {
}
