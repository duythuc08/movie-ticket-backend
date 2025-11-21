package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.MovieCreationRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.AdminMovieResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.MovieResponse;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Genre;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Movies;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.MovieMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.GenreRepository;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.MovieRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MovieService {
    MovieRepository movieRepository;
    MovieMapper movieMapper;
    private final GenreRepository genreRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse createMovie(MovieCreationRequest request){
        if(movieRepository.existsByTitle(request.getTitle())){
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        Movies movie = movieMapper.toMovies(request);
        // Convert genreNames -> Genre entities
        Set<Genre> genres = request.getGenreName().stream()
                .map(name -> genreRepository.findByName(name)
                        .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                .collect(Collectors.toSet());

        movie.setGenre(genres);
        movie.setCreatedAt(LocalDateTime.now());
        return   movieMapper.toMovieRespone(movieRepository.save(movie));
    }

    public List<MovieResponse> getMovies(){
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieRespone)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminMovieResponse> getAdminMovies(){
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toAdminMovieRespone)
                .toList();
    }
}
