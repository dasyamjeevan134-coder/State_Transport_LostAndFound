package com.transport.lostfound.controller;

import com.transport.lostfound.dto.response.ApiResponse;
import com.transport.lostfound.dto.response.ItemMatchResponse;
import com.transport.lostfound.service.ItemMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Match Engine", description = "Endpoints for retrieving automated heuristic match scores")
public class MatchingController {

    private final ItemMatchService matchService;

    public MatchingController(ItemMatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/lost-item/{lostItemId}")
    @Operation(summary = "Get suggested matches for a lost item")
    public ResponseEntity<ApiResponse<List<ItemMatchResponse>>> getMatchesForLostItem(@PathVariable Long lostItemId) {
        List<ItemMatchResponse> matches = matchService.getMatchesForLostItem(lostItemId);
        return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
    }

    @GetMapping("/found-item/{foundItemId}")
    @Operation(summary = "Get suggested matches for a found item")
    public ResponseEntity<ApiResponse<List<ItemMatchResponse>>> getMatchesForFoundItem(@PathVariable Long foundItemId) {
        List<ItemMatchResponse> matches = matchService.getMatchesForFoundItem(foundItemId);
        return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
    }
}
