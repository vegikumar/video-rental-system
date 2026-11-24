package com.crio.rentvideo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.crio.rentvideo.dto.VideoRequest;
import com.crio.rentvideo.dto.VideoResponse;
import com.crio.rentvideo.entity.Video;
import com.crio.rentvideo.exception.ResourceNotFoundException;
import com.crio.rentvideo.repository.VideoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    @Test
    void createVideo_defaultsAvailableToTrue() {
        VideoRequest request = VideoRequest.builder()
                .title("Inception")
                .director("Christopher Nolan")
                .genre("Sci-Fi")
                .build();

        when(videoRepository.save(any(Video.class))).thenAnswer(invocation -> {
            Video video = invocation.getArgument(0);
            video.setId(1L);
            return video;
        });

        VideoResponse response = videoService.createVideo(request);

        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getTitle()).isEqualTo("Inception");
    }

    @Test
    void getAvailableVideos_returnsOnlyAvailable() {
        Video available = Video.builder()
                .id(1L)
                .title("Available")
                .director("Dir")
                .genre("Action")
                .available(true)
                .build();

        when(videoRepository.findByAvailableTrue()).thenReturn(List.of(available));

        List<VideoResponse> responses = videoService.getAvailableVideos();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getTitle()).isEqualTo("Available");
    }

    @Test
    void getVideoById_throwsWhenMissing() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoService.getVideoById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteVideo_removesExistingVideo() {
        Video video = Video.builder()
                .id(5L)
                .title("To Delete")
                .director("Dir")
                .genre("Drama")
                .available(true)
                .build();

        when(videoRepository.findById(5L)).thenReturn(Optional.of(video));

        videoService.deleteVideo(5L);

        verify(videoRepository).delete(video);
    }
}
