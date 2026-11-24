package com.crio.rentvideo.controller;

import com.crio.rentvideo.dto.VideoRequest;
import com.crio.rentvideo.dto.VideoResponse;
import com.crio.rentvideo.service.VideoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping
    public ResponseEntity<List<VideoResponse>> getVideos(
            @RequestParam(defaultValue = "true") boolean availableOnly) {
        List<VideoResponse> videos = availableOnly
                ? videoService.getAvailableVideos()
                : videoService.getAllVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getVideoById(id));
    }

    @PostMapping
    public ResponseEntity<VideoResponse> createVideo(@Valid @RequestBody VideoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(videoService.createVideo(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResponse> updateVideo(
            @PathVariable Long id, @Valid @RequestBody VideoRequest request) {
        return ResponseEntity.ok(videoService.updateVideo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
