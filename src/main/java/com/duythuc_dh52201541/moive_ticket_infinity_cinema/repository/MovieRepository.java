package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Movies;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movies,String> {
    boolean existsByTitle(String title);

}
