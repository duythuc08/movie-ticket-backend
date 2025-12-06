package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.SeatShowTime;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatShowTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


public interface SeatShowTimeRepository extends JpaRepository<SeatShowTime, String> {

    @Modifying // Bắt buộc vì đây là lệnh INSERT/UPDATE
    @Query(value = "INSERT INTO seat_show_time (seat_id, show_time_id, seat_show_time_status) " +
            "SELECT s.seat_id, :showTimeId, 'AVAILABLE'" +
            "FROM seat s " +
            "WHERE s.room_id = :roomId",
            nativeQuery = true)
    void bulkInsertSeatsForShowTime(@Param("showTimeId") Long showTimeId, @Param("roomId") Long roomId);

    // --- API VẼ SƠ ĐỒ GHẾ ---
    // Lấy toàn bộ trạng thái ghế của 1 suất chiếu
    // SQL FK: show_time_id
    // SQL Table: seat_show_time
    List<SeatShowTime> findByShowTimes_ShowTimeId(Long showTimeId);

    // --- API ĐẶT VÉ (BOOKING) ---
    List<SeatShowTime> findAllBySeatShowTimeId(Long seatShowTimeId);

    // Check nhanh xem list ghế này có cái nào ĐÃ BÁN hoặc ĐANG GIỮ không?
    // SQL column: seat_show_time_status enum('AVAILABLE','BLOCKED','RESERVED','SOLD')
    @Query("SELECT COUNT(ss) > 0 FROM SeatShowTime ss " +
            "WHERE ss.seatShowTimeId IN :ids " +
            "AND ss.seatShowTimeStatus IN ('SOLD', 'RESERVED', 'BLOCKED')")
    boolean existsAnyNotAvailable(@Param("ids") List<Long> ids);

    // --- JOB QUÉT DỌN (CRON JOB) ---
    // Tìm các ghế đang giữ chỗ (RESERVED) mà đã hết hạn giữ (locked_until < now)
    // Để release lại thành AVAILABLE cho người khác mua
    List<SeatShowTime> findBySeatShowTimeStatusAndLockedUntilBefore(SeatShowTimeStatus status, LocalDateTime now);

    // (Optional) Xóa nhanh hoặc Reset status hàng loạt
    @Modifying
    @Query("UPDATE SeatShowTime ss SET ss.seatShowTimeStatus = 'AVAILABLE', ss.users = null, ss.lockedUntil = null " +
            "WHERE ss.seatShowTimeStatus = 'RESERVED' AND ss.lockedUntil < :now")
    void releaseExpiredSeats(@Param("now") LocalDateTime now);

    boolean existsBySeatShowTimeId(Long seatShowTimeId);
}
