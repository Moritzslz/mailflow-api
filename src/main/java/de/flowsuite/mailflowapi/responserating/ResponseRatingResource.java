package de.flowsuite.mailflowapi.responserating;

import de.flowsuite.mailflow.common.constant.Timeframe;
import de.flowsuite.mailflow.common.entity.ResponseRating;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
            @RequestParam String token,
            @RequestBody @Valid CreateResponseRatingRequest request,
            UriComponentsBuilder uriBuilder) {
        ResponseRating createdResponseRating =
                responseRatingService.createResponseRating(token, request);

        URI location =
                uriBuilder
                        .path("/customers/{customerId}/users/{userId}/response-ratings/{id}")
                        .buildAndExpand(
                                createdResponseRating.getCustomerId(),
                                createdResponseRating.getUserId(),
                                createdResponseRating.getMessageLogId())
                        .toUri();

        return ResponseEntity.created(location).body(createdResponseRating);
    }

    @GetMapping("/{customerId}/users/{userId}/response-ratings/{id}")
    ResponseEntity<ResponseRating> getResponseRating(
            @PathVariable long customerId,
            @PathVariable long userId,
            @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                responseRatingService.getResponseRating(customerId, userId, id, jwt));
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
