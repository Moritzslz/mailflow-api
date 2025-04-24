package de.flowsuite.mailflowapi.responserating;

import de.flowsuite.mailflowapi.common.constant.Timeframe;
import de.flowsuite.mailflowapi.common.entity.ResponseRating;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/customers")
class ResponseRatingResource {

    private final ResponseRatingService responseRatingService;

    ResponseRatingResource(ResponseRatingService responseRatingService) {
        this.responseRatingService = responseRatingService;
    }

    @PostMapping("/users/response-ratings")
    ResponseEntity<ResponseRating> createResponseRating(
            @RequestParam String token, @RequestBody @Valid CreateResponseRatingRequest request) {
        return ResponseEntity.ok(responseRatingService.createResponseRating(token, request));
    }

    @GetMapping("/{customerId}/users/response-ratings/{id}")
    ResponseEntity<ResponseRating> getResponseRatings(
            @PathVariable long customerId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(responseRatingService.getResponseRating(customerId, id, jwt));
    }

    @GetMapping("/{customerId}/response-ratings")
    ResponseEntity<List<ResponseRating>> listResponseRatings(
            @PathVariable long customerId, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                responseRatingService.listResponseRatingsByCustomer(customerId, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/response-ratings")
    ResponseEntity<List<ResponseRating>> listResponseRatings(
            @PathVariable long customerId,
            @PathVariable long userId,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                responseRatingService.listResponseRatingsByUser(customerId, userId, jwt));
    }

    @GetMapping("/{customerId}/response-ratings/analytics")
    ResponseEntity<ResponseRatingAnalyticsResponse> getResponseRatingAnalyticsForCustomer(
            @PathVariable long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Timeframe timeframe,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                responseRatingService.getResponseRatingAnalyticsForCustomer(
                        customerId, from, to, timeframe, jwt));
    }

    @GetMapping("/{customerId}/users/{userId}/response-ratings/analytics")
    ResponseEntity<ResponseRatingAnalyticsResponse> getResponseRatingAnalyticsForUser(
            @PathVariable long customerId,
            @PathVariable long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    Date from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
            @RequestParam(required = false) Timeframe timeframe,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                responseRatingService.getResponseRatingAnalyticsForUser(
                        customerId, userId, from, to, timeframe, jwt));
    }

    record CreateResponseRatingRequest(
            boolean isSatisfied, @Min(0) @Max(5) int rating, String feedback) {}

    record ResponseRatingAnalyticsResponse(
            double avgSatisfaction, double avgRating, double ratingRate) {}
}
