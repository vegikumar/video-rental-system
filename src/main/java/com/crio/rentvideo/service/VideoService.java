package com.crio.rentvideo.service;

import com.crio.rentvideo.dto.VideoRequest;
import com.crio.rentvideo.dto.VideoResponse;
import com.crio.rentvideo.entity.Video;
import com.crio.rentvideo.exception.ResourceNotFoundException;
import com.crio.rentvideo.repository.VideoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public List<VideoResponse> getAvailableVideos() {
        return videoRepository.findByAvailableTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<VideoResponse> getAllVideos() {
        return videoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public VideoResponse getVideoById(Long id) {
        return toResponse(findVideo(id));
    }

    public VideoResponse createVideo(VideoRequest request) {
        Video video = Video.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .available(request.getAvailable() == null || request.getAvailable())
                .build();
        return toResponse(videoRepository.save(video));
    }

    public VideoResponse updateVideo(Long id, VideoRequest request) {
        Video video = findVideo(id);
        video.setTitle(request.getTitle());
        video.setDirector(request.getDirector());
        video.setGenre(request.getGenre());
        if (request.getAvailable() != null) {
            video.setAvailable(request.getAvailable());
        }
        return toResponse(videoRepository.save(video));
    }

    public void deleteVideo(Long id) {
        Video video = findVideo(id);
        videoRepository.delete(video);
    }

    public Video findVideo(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
    }

    private VideoResponse toResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .director(video.getDirector())
                .genre(video.getGenre())
                .available(video.isAvailable())
                .build();
    }
}
