package com.duythuc_dh52201541.moive_ticket_infinity_cinema.repository;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.entity.Banner;
import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.BannerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner,String> {
    boolean existsByTitle(String name);
    List<Banner> findAllByOrderByPriorityAsc(); //priority tăng dần
    List<Banner> findByActiveTrueOrderByPriorityAsc(); //Lấy tất cả banner đang active, sắp xếp theo priority
    List<Banner> findByBannerTypeOrderByPriorityAsc(BannerType bannerType);


}
