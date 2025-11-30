package com.duythuc_dh52201541.moive_ticket_infinity_cinema.model_entity;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.RoomStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.RoomType;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.ShowTimeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowTimeModel {
    Long showTimeId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    ShowTimeStatus showTimeStatus;

    Long movieId;
    String movieTitle;   // tên phim

    Long roomId;
    int roomCapacity;
    String roomName;
    RoomType roomType;
    Long cinemaId;
}