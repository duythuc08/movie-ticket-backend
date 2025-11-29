package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.ShowTimeRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.ShowTimeResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.ShowTimes;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.ShowTimeStatus;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.ShowTimeMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.MovieRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.RoomRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.ShowTimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowTimeService {
    ShowTimeRepository showTimeRepository;
    ShowTimeMapper showTimeMapper;
    // Cần thêm 2 Repository này để tìm kiếm
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ShowTimeResponse createShowTime(ShowTimeRequest request) {
        // FIX: Phải check trùng lịch theo ROOM ID (Phòng chiếu), không phải Movie ID
        // Cần đảm bảo request.getRooms() không null để tránh NullPointerException (Lỗi 999)
        if (request.getRoomId() == null) {
            throw new RuntimeException("Room ID is required"); // Hoặc ném AppException phù hợp
        }

        if (showTimeRepository.existsConflictingShowtime(
                request.getRoomId(), // Đã sửa từ getMovies() thành getRooms()
                request.getStartTime(),
                request.getEndTime())) {
            throw new AppException(ErrorCode.SHOWTIME_EXISTED);
        }

        ShowTimes showTimes = showTimeMapper.toShowTimes(request);
        return showTimeMapper.toShowTimeResponse(showTimeRepository.save(showTimes));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ShowTimeResponse> createShowTimes(List<ShowTimeRequest> requests) {
        return requests.stream()
                .map(this::createShowTime) // Tận dụng lại hàm createShowTime ở trên để code gọn hơn
                .toList();
    }

    public List<ShowTimeResponse> getAllShowTimes() {
        return showTimeRepository.findAllWithDetails()
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    // 1. Lấy suất chiếu của một phim trong khoảng thời gian
    public List<ShowTimeResponse> getShowTimesByMovieAndTimeRange(Long movieId, LocalDateTime start, LocalDateTime end) {
        // Lưu ý: Kiểm tra lại tên biến trong Entity là 'movies' hay 'movie'
        return showTimeRepository.findByMovies_MovieIdAndStartTimeBetween(movieId, start, end)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    // 2. Lấy lịch chiếu của một phim tại một rạp cụ thể
    public List<ShowTimeResponse> getShowTimesByCinemaAndMovie(Long cinemaId, Long movieId, LocalDateTime now) {
        return showTimeRepository.findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(cinemaId, movieId, now)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    // 3. Lấy suất chiếu active (Fix tên biến roomId -> movieId cho đúng logic)
    public List<ShowTimeResponse> getActiveShowTimesByMovieAndRange(Long movieId, LocalDateTime start, LocalDateTime end) {
        return showTimeRepository.findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(movieId, start, end, ShowTimeStatus.CANCELLED)
                .stream()
                .sorted(Comparator.comparing(ShowTimes::getStartTime))
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    // 4. Lấy tất cả suất chiếu theo phim
    public List<ShowTimeResponse> getShowTimesByMovie(Long movieId){
        return  showTimeRepository.findByMovies_MovieId(movieId)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }
}