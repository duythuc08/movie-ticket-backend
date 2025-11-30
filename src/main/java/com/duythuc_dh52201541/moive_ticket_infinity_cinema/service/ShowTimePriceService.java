package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.ShowTimePriceRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.ShowTimePriceResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.ShowTimePrice;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.SeatType;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.ShowTimePriceMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.ShowTimePriceRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.ShowTimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ShowTimePriceService {
    ShowTimeRepository showTimeRepository;
    ShowTimePriceMapper showTimePriceMapper;
    private final ShowTimePriceRepository showTimePriceRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ShowTimePriceResponse createShowTimePrice(ShowTimePriceRequest request){
        if (showTimePriceRepository.existsByShowtimes_ShowTimeIdAndSeatType(request.getShowTimeId(),request.getSeatType())){
            throw new AppException(ErrorCode.SHOWTIME_PRICE_EXISTED);
        }
        ShowTimePrice showTimePrice = showTimePriceMapper.toShowTimePrice(request);
        return showTimePriceMapper.toShowTimePriceResponse(showTimePriceRepository.save(showTimePrice));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ShowTimePriceResponse> createShowTimePrices(List<ShowTimePriceRequest> requests){
        return requests.stream()
                .map(this::createShowTimePrice)
                .toList();
    }

    public List<ShowTimePriceResponse> getAllPriceByShowTime(Long showTimeId){
        return showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId)
                .stream()
                .map(showTimePriceMapper::toShowTimePriceResponse)
                .toList();
    }

    public ShowTimePriceResponse getPriceByShowTimeAndSeatType(Long showTimeId, SeatType seatType){
        ShowTimePrice showTimePrice = showTimePriceRepository.findByShowtimes_ShowTimeIdAndSeatType(showTimeId,seatType)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_PRICE_NOT_FOUND));
        return showTimePriceMapper.toShowTimePriceResponse(showTimePrice);
    }
}
