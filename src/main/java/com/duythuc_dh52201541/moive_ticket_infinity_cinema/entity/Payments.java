package com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="payment")
@Data // Lombok: sinh getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: sinh constructor không tham số
@AllArgsConstructor // Lombok: sinh constructor có tham số cho tất cả field
@Builder // Lombok: hỗ trợ tạo object theo Builder pattern
@FieldDefaults(level = AccessLevel.PRIVATE) // Lombok: mặc định tất cả field là private
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;
    // 🔑 Khóa chính của bảng (mã giao dịch thanh toán)

    BigDecimal amount;
    // 💰 Số tiền thanh toán (tổng tiền hoặc số tiền thực trả)


    String transactionId;
    // 🧾 Mã giao dịch từ cổng thanh toán (VD: Momo, VNPay, ZaloPay...)

    String paymentInfo;
    // 📄 Thông tin mô tả thêm về giao dịch (VD: "Thanh toán vé xem phim Avengers")

    LocalDateTime paymentDate;
    // ⏰ Ngày giờ thực hiện giao dịch

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;
    // ⚙️ Trạng thái thanh toán (PENDING, SUCCESS, FAILED, REFUNDED...)

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Orders order;
    // 🔗 Liên kết đến đơn hàng mà giao dịch này thuộc về
    // Một đơn hàng có thể có nhiều lần thanh toán (VD: thử lại nếu lỗi)
}
