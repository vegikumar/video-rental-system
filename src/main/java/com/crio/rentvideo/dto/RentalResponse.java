package com.crio.rentvideo.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalResponse {

    private Long id;
    private Long userId;
    private Long videoId;
    private String videoTitle;
    private LocalDateTime rentedAt;
    private LocalDateTime returnedAt;
    private boolean active;
}
