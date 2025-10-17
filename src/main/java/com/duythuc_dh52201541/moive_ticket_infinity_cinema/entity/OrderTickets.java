package com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatType;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="order_ticket")
@Data // Lombok: sinh getter, setter, toString, equals, hashCode
@NoArgsConstructor // Lombok: sinh constructor không tham số
@AllArgsConstructor // Lombok: sinh constructor có tham số cho tất cả field
@Builder // Lombok: hỗ trợ tạo object theo Builder pattern
@FieldDefaults(level = AccessLevel.PRIVATE) // Lombok: mặc định tất cả field là private
public class OrderTickets {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    Long orderTicketId;
    @Column(nullable = false)
    BigDecimal price;
    String qrCode;

    @Enumerated(EnumType.STRING)
    TicketStatus ticketStatus;

    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Orders orders;

    @OneToOne
    @JoinColumn(name = "seat_show_time_id",unique = true)
    SeatShowTime seatShowTime;
}
