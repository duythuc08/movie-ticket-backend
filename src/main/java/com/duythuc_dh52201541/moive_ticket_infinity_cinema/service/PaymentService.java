package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.PaymentConfirmRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.*;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.utils.QRCodeUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    OrderRepository orderRepository;
    OrderTicketRepository orderTicketRepository;
    OrderFoodRepository  orderFoodRepository;
    PaymentRepository paymentRepository;
    QRCodeUtils  qrCodeUtils;
    SeatShowTimeRepository seatShowTimeRepository;

    EmailService emailService;

    //1. Thanh toán thành công
    @Transactional
    public void processSuccess(PaymentConfirmRequest request){
        Orders order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        //a. Lưu bản ghi thanh toán
        Payments payment = Payments.builder()
                .order(order)
                .amount(order.getFinalPrice())
                .paymentDate(LocalDateTime.now())
                .transactionId(request.getTransactionId())
                .paymentInfo(request.getPaymentInfo())
                .paymentType(request.getPaymentType())
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        paymentRepository.save(payment);

        //b. cập nhật order tạo qrCode
        order.setOrderStatus(OrderStatus.PAID);
        String qrCode = "INF-" + order.getOrderId() + "-" + System.currentTimeMillis() % 10000;
        String qrBase64 = qrCodeUtils.generateQRCodeImage(qrCode,300,300);
        order.setQrCode(qrCode);
        orderRepository.save(order);

        //c. Cập nhật vé và ghế
        List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
        for (OrderTickets t : tickets) {
            t.setTicketStatus(TicketStatus.CONFIRMED);

            // Cập nhật ghế thành ĐÃ BÁN (Không ai mua được nữa)
            SeatShowTime sst = t.getSeatShowTime();
            sst.setSeatShowTimeStatus(SeatShowTimeStatus.SOLD);
            sst.setLockedUntil(null);
            seatShowTimeRepository.save(sst);
        }
        orderTicketRepository.saveAll(tickets);

        //.d Gửi mail thông báo đặt vé
        if(order.getUsers() != null) {
            sendPaymentSuccessMail(order.getUsers(), order, qrCode, qrBase64);
        }
    }

    //2. Thanh toán thất bại
    @Transactional
    public void processFail(Orders order){
        //a. Cập nhật Order trạng thái CANCELLED
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        //b. Cập nhật Ticket & TRẢ GHẾ (RELEASE)
        List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
        for (OrderTickets ticket : tickets) {
            ticket.setTicketStatus(TicketStatus.CANCELLED);

            SeatShowTime sst = ticket.getSeatShowTime();
            sst.setSeatShowTimeStatus(SeatShowTimeStatus.AVAILABLE); // Nhả ghế về trạng thái trống
            sst.setLockedUntil(null); // Xóa thời gian giữ ghế
            seatShowTimeRepository.save(sst);
        }
        orderTicketRepository.saveAll(tickets);
    }

    private void sendPaymentSuccessMail(Users user, Orders order, String bookingCode, String qrBase64) {
        // Nội dung HTML email
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #fff; padding: 20px; border-radius: 8px; "
                + "box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h2 style=\"color: #007bff; text-align: center;\">Thanh toán thành công 🎉</h2>"
                + "<p>Xin chào <b>" + user.getFirstname() + " " + user.getLastname() + "</b>,</p>"
                + "<p>Cảm ơn bạn đã đặt vé tại <b>Infinity Cinema</b>. Dưới đây là thông tin đơn hàng của bạn:</p>"
                + "<ul>"
                + "<li><b>Mã đơn hàng:</b> " + order.getOrderId() + "</li>"
                + "<li><b>Mã booking:</b> " + bookingCode + "</li>"
                + "<li><b>Số tiền:</b> " + order.getFinalPrice() + " VND</li>"
                + "<li><b>Thời gian thanh toán:</b> " + LocalDateTime.now() + "</li>"
                + "</ul>"
                + "<p>Vui lòng sử dụng mã QR dưới đây để check-in tại rạp:</p>"
                + "<div style=\"text-align: center; margin: 20px;\">"
                + "<img src=\"data:image/png;base64," + qrBase64 + "\" alt=\"QR Code\" style=\"width:200px;height:200px;\"/>"
                + "</div>"
                + "<p style=\"text-align: center; font-size: 14px; color: #555;\">Chúc bạn có trải nghiệm xem phim tuyệt vời!</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getUsername(), "Xác nhận thanh toán thành công", htmlMessage);
            log.info("Payment success email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment success email", e);
        }
    }

}
