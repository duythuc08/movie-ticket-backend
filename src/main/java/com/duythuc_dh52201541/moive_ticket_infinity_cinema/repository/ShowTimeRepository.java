package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.ShowTimes;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.ShowTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowTimeRepository extends JpaRepository<ShowTimes, String> {

    // Tương tự cho hàm lấy tất cả (thay thế findAll)
    @Query("SELECT s FROM ShowTimes s " +
            "LEFT JOIN FETCH s.movies " +
            "LEFT JOIN FETCH s.rooms r " +
            "LEFT JOIN FETCH r.cinemas")
    List<ShowTimes> findAllWithDetails();



    List<ShowTimes> findByMovies_MovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime);

    // Cách viết Query thuần để tránh lỗi JPA naming convention (Recommended)
    @Query("SELECT s FROM ShowTimes s " +
            "WHERE s.rooms.cinemas.cinemaId = :cinemaId " +
            "AND s.movies.movieId = :movieId " +
            "AND s.startTime > :now")
    List<ShowTimes> findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId") Long movieId,
            @Param("now") LocalDateTime now);


    List<ShowTimes> findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(
            Long movieId,
            LocalDateTime start,
            LocalDateTime end,
            ShowTimeStatus status
    );

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM ShowTimes s " +
            "WHERE s.rooms.roomId = :roomId " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "AND (:newItemStart < s.endTime AND :newItemEnd > s.startTime)")
    boolean existsConflictingShowtime(@Param("roomId") Long roomId,
                                      @Param("newItemStart") LocalDateTime newItemStart,
                                      @Param("newItemEnd") LocalDateTime newItemEnd);

    List<ShowTimes> findByMovies_MovieId(Long movieId);
}