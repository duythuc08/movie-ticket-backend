package com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.MovieCreationRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.AdminMovieRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.MovieRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Movies;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    Movies toMovies(MovieCreationRequest request);

    MovieRespone  toMovieRespone(Movies movies);

    AdminMovieRespone toAdminMovieRespone(Movies movies);

}
