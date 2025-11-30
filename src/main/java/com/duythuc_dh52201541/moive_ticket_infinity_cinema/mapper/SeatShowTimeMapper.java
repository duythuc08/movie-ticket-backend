package com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.SeatShowTimeRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.SeatShowTimeResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.SeatShowTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatShowTimeMapper {

    SeatShowTime toSeatShowTime (SeatShowTimeRequest  request);

    // 1. Map thông tin User (Lưu ý: User có thể null nếu ghế chưa ai đặt)
    @Mapping(source = "users.userId", target = "userId")

    // 2. Map thông tin Ghế (Flatten từ object Seats)
    @Mapping(source = "seats.seatId", target = "seatId")
    @Mapping(source = "seats.seatRow", target = "seatRow")
    @Mapping(source = "seats.seatNumber", target = "seatNumber")
    @Mapping(source = "seats.seatType", target = "seatType")


    // 4. Map thông tin Suất chiếu & Phòng (Flatten từ object ShowTimes & Rooms)
    @Mapping(source = "showTimes.showTimeId", target = "showTimeId")
    // Lấy thông tin phòng thông qua ShowTimes (vì ShowTime gắn với Room)
    @Mapping(source = "showTimes.rooms.roomId", target = "roomId")
    @Mapping(source = "showTimes.rooms.name", target = "roomName")
    @Mapping(source = "showTimes.rooms.roomType", target = "roomType")
    SeatShowTimeResponse toSeatShowTimeResponse(SeatShowTime seatShowTime);
}
