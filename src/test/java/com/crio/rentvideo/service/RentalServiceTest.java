package com.crio.rentvideo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.crio.rentvideo.dto.RentalResponse;
import com.crio.rentvideo.entity.Rental;
import com.crio.rentvideo.entity.Role;
import com.crio.rentvideo.entity.User;
import com.crio.rentvideo.entity.Video;
import com.crio.rentvideo.exception.BadRequestException;
import com.crio.rentvideo.repository.RentalRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserService userService;

    @Mock
    private VideoService videoService;

    @InjectMocks
    private RentalService rentalService;

    private User customer;
    private Video video;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .email("customer@example.com")
                .password("hashed")
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.CUSTOMER)
                .build();

        video = Video.builder()
                .id(10L)
                .title("The Matrix")
                .director("Wachowski")
                .genre("Sci-Fi")
                .available(true)
                .build();
    }

    @Test
    void rentVideo_marksVideoUnavailable() {
        when(userService.getById(1L)).thenReturn(customer);
        when(rentalRepository.countByUserIdAndActiveTrue(1L)).thenReturn(0L);
        when(videoService.findVideo(10L)).thenReturn(video);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
            Rental rental = invocation.getArgument(0);
            rental.setId(100L);
            return rental;
        });

        RentalResponse response = rentalService.rentVideo(1L, 10L, "customer@example.com");

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(video.isAvailable()).isFalse();
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void rentVideo_rejectsWhenLimitReached() {
        when(userService.getById(1L)).thenReturn(customer);
        when(rentalRepository.countByUserIdAndActiveTrue(1L)).thenReturn(2L);

        assertThatThrownBy(() -> rentalService.rentVideo(1L, 10L, "customer@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum video rental limit");
    }

    @Test
    void returnVideo_makesVideoAvailableAgain() {
        video.setAvailable(false);
        Rental rental = Rental.builder()
                .id(100L)
                .user(customer)
                .video(video)
                .rentedAt(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();

        when(rentalRepository.findById(100L)).thenReturn(java.util.Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse response = rentalService.returnVideo(100L, "customer@example.com");

        assertThat(response.isActive()).isFalse();
        assertThat(video.isAvailable()).isTrue();
        assertThat(response.getReturnedAt()).isNotNull();
    }
}
