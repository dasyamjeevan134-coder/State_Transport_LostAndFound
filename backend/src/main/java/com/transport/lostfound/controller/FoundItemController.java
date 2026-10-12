package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.FoundItemCreateRequest;
import com.transport.lostfound.dto.request.FoundItemUpdateRequest;
import com.transport.lostfound.dto.response.*;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.ClaimService;
import com.transport.lostfound.service.FoundItemService;
import com.transport.lostfound.service.ItemMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/found-items")
@Tag(name = "Found Items", description = "Endpoints for managing recovered items, searches, claims, and matches")
public class FoundItemController {

    private final FoundItemService foundItemService;
    private final ItemMatchService matchService;
    private final ClaimService claimService;

    public FoundItemController(FoundItemService foundItemService,
                               ItemMatchService matchService,
                               ClaimService claimService) {
        this.foundItemService = foundItemService;
        this.matchService = matchService;
        this.claimService = claimService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Register an item recovered during transit (Staff/Admin only)")
    public ResponseEntity<ApiResponse<FoundItemResponse>> createFoundItem(
            @Valid @RequestBody FoundItemCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FoundItemResponse response = foundItemService.createFoundItem(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Found item registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search and filter recovered found items")
    public ResponseEntity<ApiResponse<PagedResponse<FoundItemResponse>>> getFoundItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) TransportType transportType,
            @RequestParam(required = false) FoundItemStatus status,
            @RequestParam(required = false) String location,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<FoundItemResponse> response = foundItemService.getFoundItems(keyword, category, transportType, status, location, pageable);
        return ResponseEntity.ok(ApiResponse.success("Found items retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recovered found item details by ID")
    public ResponseEntity<ApiResponse<FoundItemResponse>> getFoundItemById(@PathVariable Long id) {
        FoundItemResponse response = foundItemService.getFoundItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Found item retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Update found item information or status (Staff/Admin only)")
    public ResponseEntity<ApiResponse<FoundItemResponse>> updateFoundItem(
            @PathVariable Long id,
            @Valid @RequestBody FoundItemUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        FoundItemResponse response = foundItemService.updateFoundItem(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Found item updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Delete found item listing (Staff/Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteFoundItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        foundItemService.deleteFoundItem(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Found item deleted successfully", null));
    }

    @GetMapping("/{id}/matches")
    @Operation(summary = "Get heuristic match suggestions for this found item against lost items")
    public ResponseEntity<ApiResponse<List<ItemMatchResponse>>> getMatchesForFoundItem(@PathVariable Long id) {
        List<ItemMatchResponse> matches = matchService.getMatchesForFoundItem(id);
        return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
    }

    @GetMapping("/{id}/claims")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get all ownership claims submitted for this found item (Staff/Admin only)")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> getClaimsForFoundItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ClaimResponse> claims = claimService.getClaimsForFoundItem(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Claims retrieved successfully", claims));
    }
}
