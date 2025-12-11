package com. duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderFoodResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema. dto.respone.OrderTicketResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception. ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental. FieldDefaults;
import lombok. extern.slf4j.Slf4j;
import org.springframework.stereotype. Service;

import java.util. ArrayList;
import java.util. List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel. PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;

    public OrderResponse getOrderById(Long orderId) {
        try {
            log.info("=== START getOrderById:  {} ===", orderId);

            // 1. Tìm order
            Orders order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
            log.info("Step 1: Found order: {}", order.getOrderId());

            // 2. Lấy user info
            String userId = null;
            String fullName = "---";
            if (order. getUsers() != null) {
                userId = order. getUsers().getUserId();
                fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
            }
            log. info("Step 2: User - {}", fullName);

            // 3. Map tickets (Set<OrderTickets>)
            List<OrderTicketResponse> ticketResponses = new ArrayList<>();
            if (order.getOrderTickets() != null && !order.getOrderTickets().isEmpty()) {
                log.info("Step 3: Mapping {} tickets", order.getOrderTickets().size());

                for (OrderTickets ticket :  order.getOrderTickets()) {
                    log.info("  Processing ticket:  {}", ticket.getOrderTicketId());

                    if (ticket.getSeatShowTime() != null && ticket.getSeatShowTime().getSeats() != null) {
                        Seats seat = ticket.getSeatShowTime().getSeats();
                        log.info("  Seat: row={}, number={}, type={}",
                                seat. getSeatRow(), seat.getSeatNumber(), seat.getSeatType());

                        OrderTicketResponse ticketResponse = OrderTicketResponse. builder()
                                .orderTicketId(ticket.getOrderTicketId())
                                . seatName(seat.getSeatRow() + String.valueOf(seat.getSeatNumber()))
                                .seatType(seat.getSeatType())
                                .price(ticket.getPrice())
                                .build();

                        ticketResponses.add(ticketResponse);
                        log.info("  Ticket mapped successfully");
                    }
                }
            }
            log.info("Step 3 DONE: {} tickets mapped", ticketResponses.size());

            // 4. Map foods (Set<OrderFoods>)
            List<OrderFoodResponse> foodResponses = new ArrayList<>();
            if (order.getOrderFoods() != null && !order.getOrderFoods().isEmpty()) {
                log.info("Step 4: Mapping {} foods", order.getOrderFoods().size());

                for (OrderFoods food : order.getOrderFoods()) {
                    log.info("  Processing food.. .");

                    if (food.getFoods() != null) {
                        log. info("  Food: name={}", food.getFoods().getName());

                        OrderFoodResponse foodResponse = OrderFoodResponse.builder()
                                .foodId(food.getFoods().getFoodId())
                                .name(food.getFoods().getName())
                                .quantity(food. getQuantity())
                                . unitPrice(food.getUnitPrice())
                                .totalPrice(food.getTotalPrice())
                                .build();

                        foodResponses.add(foodResponse);
                        log.info("  Food mapped successfully");
                    }
                }
            }
            log.info("Step 4 DONE: {} foods mapped", foodResponses.size());

            // 5. Build response
            log.info("Step 5: Building response...");
            OrderResponse response = OrderResponse. builder()
                    .orderId(order.getOrderId())
                    .userId(userId)
                    .fullName(fullName)
                    .totalTicketPrice(order.getTotalTicketPrice())
                    .totalFoodPrice(order.getTotalFoodPrice())
                    .discountAmount(order.getDiscountAmount())
                    .finalPrice(order.getFinalPrice())
                    .promotionCode(order.getPromotionCode())
                    .orderStatus(order. getOrderStatus())
                    . bookingTime(order.getBookingTime())
                    .expiredTime(order.getExpiredTime())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .qrCode(order.getQrCode())
                    .tickets(ticketResponses)
                    .foods(foodResponses)
                    . build();

            log.info("=== SUCCESS getOrderById: {} ===", orderId);
            return response;

        } catch (AppException e) {
            log.error("AppException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== ERROR getOrderById ===");
            log.error("Exception type: {}", e. getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace: ", e);
            throw new RuntimeException("Error getting order:  " + e.getMessage(), e);
        }
    }
}