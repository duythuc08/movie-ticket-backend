package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.BookingRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.OrderFoodsRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderFoodResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.OrderTicketResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    OrderRepository orderRepository;
    OrderTicketRepository  orderTicketRepository;
    OrderFoodRepository orderFoodRepository;
    SeatShowTimeRepository seatShowTimeRepository;
    FoodRepository foodRepository;
    UserRepository userRepository;
    ShowTimePriceService showTimePriceService;
    PromotionRepository promotionRepository;

    private static final int HOLD_SEAT_MINUTES = 5;
    @Transactional(rollbackOn =  Exception.class)
    public OrderResponse createBooking(BookingRequest request){
        // 1. LẤY THỜI GIAN CHUẨN (Dùng chung cho toàn bộ hàm)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusMinutes(HOLD_SEAT_MINUTES);

        //1. Kiểm tra user đặt vé
        Users user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //2. Kiểm tra và lock ghế được chọn
        List<SeatShowTime> seats = seatShowTimeRepository.findAllBySeatShowTimeIdIn(request.getSeatShowTimeId());
        if(seats.size() != request.getSeatShowTimeId().size()){
            throw new RuntimeException("Dữ liệu ghế không hợp lệ");
        }
        for (SeatShowTime seat : seats) {
            if(!seat.getSeatShowTimeStatus().equals(SeatShowTimeStatus.AVAILABLE))
                throw new RuntimeException("Ghế " + seat.getSeats().getSeatRow() + seat.getSeats().getSeatNumber() + " đã có người đặt!");
            seat.setSeatShowTimeStatus(SeatShowTimeStatus.RESERVED);
            seat.setLockedUntil(now.plusMinutes(5));
        }
        seatShowTimeRepository.saveAll(seats);

        //3. Tạo order với trạng trạng thái đang chờ thanh toán
        Orders order = Orders.builder()
                .users(user)
                .bookingTime(now)
                .createdAt(now)
                .expiredTime(now.plusMinutes(5))
                .orderStatus(OrderStatus.PENDING)
                .build();
        orderRepository.save(order);

        //4. Tạo orderTicket
        BigDecimal totalTicketPrice = BigDecimal.ZERO;
        List<OrderTickets> tickets = new ArrayList<>();

            //Lấy ID suất chiếu, do khach chỉ được phép đặt nhiều ghe trong cùng 1 suất chiếu
        Long showTimeId = seats.get(0).getShowTimes().getShowTimeId();
            //Gọi service lấy giá của các loại ghế trong suất chiếu đó 1 lần
        Map<SeatType, BigDecimal> priceMap = showTimePriceService.getPriceMapByShowTime(showTimeId);
        for (SeatShowTime seat : seats) {
            //a. Xác định loại ghế được chọn trong suất chiếu là loại ghế gì
            SeatType currentSeatType = seat.getSeats().getSeatType();

            //b. lấy giá tiền của theo ghế
            BigDecimal price =  priceMap.get(currentSeatType);
            // Phòng hờ: Nếu Map trả về null (tức là rạp quên set giá cho loại ghế này)
            if (price == null) {
                throw new RuntimeException("Lỗi hệ thống: Không tìm thấy giá cho ghế loại " + currentSeatType);
            }

            //c. Tạo vé với giá vừa lấy

            OrderTickets ticket = OrderTickets.builder()
                    .orders(order)
                    .seatShowTime(seat)
                    .price(price)
                    .ticketStatus(TicketStatus.RESERVED)
                    .createdAt(now)
                    .build();
            tickets.add(ticket);
            totalTicketPrice = totalTicketPrice.add(price);
        }
        orderTicketRepository.saveAll(tickets);

        //5. Tạo oderFood nếu khách có chọn thêm đồ ăn
        List<OrderFoods> orderFoods = new ArrayList<>();
        BigDecimal totalFoodPrice = BigDecimal.ZERO;
        if(request.getFoods() != null){
            for (OrderFoodsRequest foodReq : request.getFoods()) {
                Foods food =foodRepository.findByFoodId(foodReq.getFoodId())
                        .orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
                BigDecimal totalItem = food.getPrice().multiply(BigDecimal.valueOf(foodReq.getQuantity()));

                OrderFoods item = OrderFoods.builder()
                        .orders(order)
                        .foods(food)
                        .quantity(foodReq.getQuantity())
                        .unitPrice(food.getPrice())
                        .totalPrice(totalItem)
                        .build();
                orderFoods.add(item);
                totalFoodPrice = totalFoodPrice.add(totalItem);
            }
        }
        orderFoodRepository.saveAll(orderFoods);

        //6. Sử dụng mã giảm giá
        BigDecimal provisionalTotal = totalTicketPrice.add(totalFoodPrice);
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedPromotionCode = null;
            //Xử lý tổng tiền khi khách su dụng mã giảm giá
        if( request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()){
            // a. Tìm mã trong database
            Promotion promotion = promotionRepository.findByCode(request.getPromotionCode())
                    .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

            // b. Validate mã (Hết hạn, hết lượt dùng, chưa đạt giá trị tối thiểu)
            if (promotion.getEndTime().isBefore(now) || promotion.getStartTime().isAfter(now)) {
                throw new AppException(ErrorCode.PROMOTION_EXPIRED);
            }

            if (promotion.getUseLimit() <= 0) {
                throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
            }

            if (provisionalTotal.compareTo(promotion.getMinOrderValue()) < 0) {
                throw new AppException(ErrorCode.PROMOTION_CONDITION_NOT_MET); // Chưa đủ tiền tối thiểu
            }

            // c. Tính toán số tiền được giảm
            if (promotion.getType().equals((PromotionType.PERCENTAGE))){
                // Ví dụ: Giảm 10% -> total * 0.1
                BigDecimal percentage = promotion.getDiscountValue().divide(new BigDecimal(100));
                discountAmount = provisionalTotal.multiply(percentage);
                // Nếu có maxDiscountAmount thì phải check
                if (promotion.getMaxDiscountAmount() != null && discountAmount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
                    discountAmount = promotion.getMaxDiscountAmount();
                }
                // (Tùy chọn) Kiểm tra giảm tối đa bao nhiêu tiền (Max discount amount) nếu cần
            }   else if (promotion.getType().equals(PromotionType.FIXED_AMOUNT)) {
                // Ví dụ: Giảm thẳng 50k
                discountAmount = promotion.getDiscountValue();
            }

            // Đảm bảo tiền giảm không vượt quá tổng tiền đơn hàng
            if (discountAmount.compareTo(provisionalTotal) > 0) {
                discountAmount = provisionalTotal;
            }

            // d. Trừ số lượng mã giảm giá (Nếu cần quản lý số lượng)
            promotion.setUseLimit(promotion.getUseLimit() - 1);
            promotionRepository.save(promotion);
            appliedPromotionCode = promotion.getCode();
        }

        // 7. Cập nhật và lưu Order cuối cùng
        BigDecimal finalPrice = provisionalTotal.subtract(discountAmount);

        order.setTotalFoodPrice(totalFoodPrice);
        order.setTotalTicketPrice(totalTicketPrice);
        order.setDiscountAmount(discountAmount);      // Set tiền giảm
        order.setPromotionCode(appliedPromotionCode); // Set mã đã dùng
        order.setFinalPrice(finalPrice);              // Set tiền phải trả cuối cùng
        order.setUpdatedAt(now);
        // Sau khi save xong, map tickets sang response
        List<OrderTicketResponse> ticketResponses = tickets.stream()
                .map(ticket -> OrderTicketResponse.builder()
                        .orderTicketId(ticket. getOrderTicketId())
                        .seatName(ticket.getSeatShowTime().getSeats().getSeatRow()+ticket.getSeatShowTime().getSeats().getSeatNumber())
                        .price(ticket.getPrice())
                        .seatType(ticket.getSeatShowTime().getSeats().getSeatType())
                        .build())
                .toList();

        // Map foods sang response
        List<OrderFoodResponse> foodResponses = orderFoods.stream()
                .map(food -> OrderFoodResponse.builder()
                        .foodId(food.getFoods().getFoodId())
                        .name(food.getFoods().getName())
                        .quantity(food.getQuantity())
                        .unitPrice(food.getUnitPrice())
                        .totalPrice(food.getTotalPrice())
                        .build())
                .toList();

        Orders savedOrder = orderRepository.save(order);

        // Trả về response ĐẦY ĐỦ
        return OrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .userId(savedOrder.getUsers().getUserId())
                .fullName(savedOrder.getUsers().getFirstname() + " " + savedOrder.getUsers().getLastname())
                .totalTicketPrice(savedOrder.getTotalTicketPrice())
                .totalFoodPrice(savedOrder.getTotalFoodPrice())
                .promotionCode(savedOrder.getPromotionCode())
                .discountAmount(savedOrder.getDiscountAmount())
                .finalPrice(savedOrder.getFinalPrice())
                .bookingTime(savedOrder.getBookingTime())
                .expiredTime(savedOrder.getExpiredTime())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .orderStatus(savedOrder.getOrderStatus())
                // ========== THÊM 2 DÒNG NÀY ==========
                .tickets(ticketResponses)
                .foods(foodResponses)
                // =====================================
                .qrCode(savedOrder.getQrCode())
                .build();
    }
}
