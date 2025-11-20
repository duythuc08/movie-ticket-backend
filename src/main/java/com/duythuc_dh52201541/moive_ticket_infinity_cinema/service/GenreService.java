package com.duythuc_dh52201541.moive_ticket_infinity_cinema.service;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.request.GenreCreationRequest;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone.GenreRespone;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Genre;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.AppException;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.exception.ErrorCode;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.mapper.GenreMapper;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository.GenreRepository;
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
public class GenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public List<GenreRespone> getGenres()
    {
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toGenreRespone).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public GenreRespone createGenre(GenreCreationRequest request){
        if(genreRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        Genre genre = genreMapper.toGenre(request);
        return genreMapper.toGenreRespone(genreRepository.save(genre));
    }
}
