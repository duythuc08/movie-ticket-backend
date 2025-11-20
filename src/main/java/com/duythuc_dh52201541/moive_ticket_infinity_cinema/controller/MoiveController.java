package com.duythuc_dh52201541.moive_ticket_infinity_cinema.controller;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.MovieCreationRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.AdminMovieRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.ApiResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.MovieRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.service.MovieService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MoiveController {
    MovieService movieService;

    @PostMapping
    ApiResponse<MovieRespone> createMovie(@RequestBody @Valid MovieCreationRequest request){
        return ApiResponse.<MovieRespone>builder()
                .result(movieService.createMovie(request))
                .message("Thêm phim mới thành công")
                .build();
    }

    @GetMapping
    ApiResponse<List<AdminMovieRespone>> getAdminMovie(){
        return ApiResponse.<List<AdminMovieRespone>>builder()
                .result(movieService.getAdminMovies())
                .build();
    }

    @GetMapping("/getMovies")
    ApiResponse<List<MovieRespone>> getMovies(){
        return ApiResponse.<List<MovieRespone>>builder()
                .result(movieService.getMovies())
                .build();
    }
}
