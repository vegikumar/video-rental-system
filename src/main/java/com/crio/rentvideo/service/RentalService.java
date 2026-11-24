package com.crio.rentvideo.service;

import com.crio.rentvideo.dto.RentalResponse;
import com.crio.rentvideo.entity.Rental;
import com.crio.rentvideo.entity.User;
import com.crio.rentvideo.entity.Video;
import com.crio.rentvideo.exception.BadRequestException;
import com.crio.rentvideo.exception.ResourceNotFoundException;
import com.crio.rentvideo.exception.UnauthorizedException;
import com.crio.rentvideo.repository.RentalRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private static final int MAX_ACTIVE_RENTALS = 2;

    private final RentalRepository rentalRepository;
    private final UserService userService;
    private final VideoService videoService;

    @Transactional
    public RentalResponse rentVideo(Long userId, Long videoId, String authenticatedEmail) {
        User user = userService.getById(userId);
        ensureSameUser(user, authenticatedEmail);

        if (rentalRepository.countByUserIdAndActiveTrue(userId) >= MAX_ACTIVE_RENTALS) {
            throw new BadRequestException(
                    "User has already reached maximum video rental limit of " + MAX_ACTIVE_RENTALS);
        }

        Video video = videoService.findVideo(videoId);
        if (!video.isAvailable()) {
            throw new BadRequestException("Video is not available for rent: " + video.getTitle());
        }

        video.setAvailable(false);

        Rental rental = Rental.builder()
                .user(user)
                .video(video)
                .rentedAt(LocalDateTime.now())
                .active(true)
                .build();

        return toResponse(rentalRepository.save(rental));
    }

    @Transactional
    public RentalResponse returnVideo(Long rentalId, String authenticatedEmail) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with id: " + rentalId));

        ensureSameUser(rental.getUser(), authenticatedEmail);

        if (!rental.isActive()) {
            throw new BadRequestException("Rental is already returned");
        }

        rental.setActive(false);
        rental.setReturnedAt(LocalDateTime.now());
        rental.getVideo().setAvailable(true);

        return toResponse(rentalRepository.save(rental));
    }

    public List<RentalResponse> getActiveRentals(Long userId, String authenticatedEmail) {
        User user = userService.getById(userId);
        ensureSameUser(user, authenticatedEmail);

        return rentalRepository.findByUserIdAndActiveTrue(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureSameUser(User user, String authenticatedEmail) {
        if (!user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            throw new UnauthorizedException("You can only manage your own rentals");
        }
    }

    private RentalResponse toResponse(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .userId(rental.getUser().getId())
                .videoId(rental.getVideo().getId())
                .videoTitle(rental.getVideo().getTitle())
                .rentedAt(rental.getRentedAt())
                .returnedAt(rental.getReturnedAt())
                .active(rental.isActive())
                .build();
    }
}
