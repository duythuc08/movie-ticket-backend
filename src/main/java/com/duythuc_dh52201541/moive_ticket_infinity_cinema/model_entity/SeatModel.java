package com.duythuc_dh52201541.moive_ticket_infinity_cinema.model_entity;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatModel {
    Long seatId;
    String seatRow;
    Integer seatNumber;
    SeatStatus  seatStatus;
    SeatType seatType;   // ví dụ: VIP, Standard
    Long roomId;
}