package com.crio.rentvideo.controller;

import com.crio.rentvideo.dto.RentalResponse;
import com.crio.rentvideo.service.RentalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/users/{userId}/videos/{videoId}")
    public ResponseEntity<RentalResponse> rentVideo(
            @PathVariable Long userId,
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalService.rentVideo(userId, videoId, userDetails.getUsername()));
    }

    @PutMapping("/{rentalId}/return")
    public ResponseEntity<RentalResponse> returnVideo(
            @PathVariable Long rentalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(rentalService.returnVideo(rentalId, userDetails.getUsername()));
    }

    @GetMapping("/users/{userId}/active")
    public ResponseEntity<List<RentalResponse>> getActiveRentals(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(rentalService.getActiveRentals(userId, userDetails.getUsername()));
    }
}
