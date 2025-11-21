package com.duythuc_dh52201541.moive_ticket_infinity_cinema.dto.respone;

import com.duythuc_dh52201541.moive_ticket_infinity_cinema.enums.BannerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerResponse {
    private Long id;
    private String imageUrl;
    private String title;
    private String description;
    private String linkUrl;
    private Integer priority;
    private Boolean active;

    private BannerType type;     // MOVIE, EVENT, ADVERTISEMENT

    private Long movieId;        // nếu có phim
    private String movieTitle;   // tên phim (để FE hiển thị nút đặt vé)

    private Long eventId;        // nếu có sự kiện
    private String eventTitle;   // tên sự kiện (để FE hiển thị thông tin)
}
