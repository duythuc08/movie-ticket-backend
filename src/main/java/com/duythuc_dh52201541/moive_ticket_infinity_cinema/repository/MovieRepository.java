package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Movies;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movies,String> {
    boolean existsByTitle(String title);
    List<Movies> findByMovieStatus(MovieStatus movieStatus);
    List<Movies> findByTitleContainingIgnoreCase(String keyword);

}
