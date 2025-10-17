package com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums;

public enum PaymentStatus {
//    VNPAY,       // VNPay - Ví điện tử VNPay
//    MOMO,        // MoMo - Ví điện tử MoMo
//    ZALOPAY,     // ZaloPay - Ví điện tử ZaloPay
//    CASH,        // Tiền mặt - Thanh toán tại quầy
//    CREDIT_CARD  // Thẻ tín dụng - Visa, Mastercard
    PENDING,   // Đang chờ - Đang chờ xử lý thanh toán
    SUCCESS,   // Thành công - Thanh toán thành công
    FAILED,    // Thất bại - Thanh toán thất bại
    REFUNDED   // Đã hoàn tiền - Đã hoàn lại tiền cho khách
}
