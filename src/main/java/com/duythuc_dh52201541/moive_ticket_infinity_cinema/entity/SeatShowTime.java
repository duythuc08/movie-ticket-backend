package com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatShowTimeStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor // Lombok: sinh constructor không tham số
@AllArgsConstructor // Lombok: sinh constructor có tham số cho tất cả field
@Builder // Lombok: hỗ trợ tạo object theo Builder pattern
@FieldDefaults(level = AccessLevel.PRIVATE) // Lombok: mặc định tất cả field là private
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeatShowTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long seatShowTimeId;
    LocalDateTime lockedUntil;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users users;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    Seats seats;

    @ManyToOne
    @JoinColumn(name = "show_time_id")
    ShowTimes showTimes;

    @OneToOne(mappedBy = "seatShowTime")
    OrderTickets orderTicket;

    @Enumerated(EnumType.STRING)
    SeatShowTimeStatus seatShowTimeStatus;
}
