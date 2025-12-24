package com. duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderFoodResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema. dto.respone.OrderTicketResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception. ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.OrderMapper;
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
    OrderMapper orderMapper;


    public OrderResponse getOrderById(Long orderId) {
        try {

            // 1. Tìm order
            Orders order = orderRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));


            // 2. Lấy user info
            String userId = null;
            String fullName = "---";
            if (order. getUsers() != null) {
                userId = order. getUsers().getUserId();
                fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
            }


            // 3. Map tickets (Set<OrderTickets>)
            List<OrderTicketResponse> ticketResponses = new ArrayList<>();
            if (order.getOrderTickets() != null && !order.getOrderTickets().isEmpty()) {


                for (OrderTickets ticket :  order.getOrderTickets()) {


                    if (ticket.getSeatShowTime() != null && ticket.getSeatShowTime().getSeats() != null) {
                        Seats seat = ticket.getSeatShowTime().getSeats();

                        OrderTicketResponse ticketResponse = OrderTicketResponse. builder()
                                .orderTicketId(ticket.getOrderTicketId())
                                . seatName(seat.getSeatRow() + String.valueOf(seat.getSeatNumber()))
                                .seatType(seat.getSeatType())
                                .price(ticket.getPrice())
                                .build();

                        ticketResponses.add(ticketResponse);
                    }
                }
            }

            // 4. Map foods (Set<OrderFoods>)
            List<OrderFoodResponse> foodResponses = new ArrayList<>();
            if (order.getOrderFoods() != null && !order.getOrderFoods().isEmpty()) {

                for (OrderFoods food : order.getOrderFoods()) {

                    if (food.getFoods() != null) {

                        OrderFoodResponse foodResponse = OrderFoodResponse.builder()
                                .foodId(food.getFoods().getFoodId())
                                .name(food.getFoods().getName())
                                .quantity(food. getQuantity())
                                . unitPrice(food.getUnitPrice())
                                .totalPrice(food.getTotalPrice())
                                .build();

                        foodResponses.add(foodResponse);
                    }
                }
            }

            // 5. Build response
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
                    .bookingTime(order.getBookingTime())
                    .expiredTime(order.getExpiredTime())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .qrCode(order.getQrCode())
                    .tickets(ticketResponses)
                    .foods(foodResponses)
                    .build();

            return response;

        } catch (AppException e) {
            log.error("AppException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error getting order:  " + e.getMessage(), e);
        }
    }
    public List<OrderResponse> getOrdersByUserId(String userId){
        return orderRepository.findByUsers_UserId(userId)
                .stream()
                .map(orderMapper :: toOrderResponse)
                .toList();
    }
}