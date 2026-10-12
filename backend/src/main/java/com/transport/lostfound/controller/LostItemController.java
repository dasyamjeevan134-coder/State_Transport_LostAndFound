package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.LostItemCreateRequest;
import com.transport.lostfound.dto.request.LostItemUpdateRequest;
import com.transport.lostfound.dto.response.ApiResponse;
import com.transport.lostfound.dto.response.ItemMatchResponse;
import com.transport.lostfound.dto.response.LostItemResponse;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.ItemMatchService;
import com.transport.lostfound.service.LostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
@Tag(name = "Lost Items", description = "Endpoints for reporting, viewing, searching, updating, and matching lost items")
public class LostItemController {

    private final LostItemService lostItemService;
    private final ItemMatchService matchService;

    public LostItemController(LostItemService lostItemService, ItemMatchService matchService) {
        this.lostItemService = lostItemService;
        this.matchService = matchService;
    }

    @PostMapping
    @Operation(summary = "Report a new lost item listing")
    public ResponseEntity<ApiResponse<LostItemResponse>> createLostItem(
            @Valid @RequestBody LostItemCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LostItemResponse response = lostItemService.createLostItem(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lost item reported successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search and filter lost items with pagination and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<LostItemResponse>>> getLostItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemCategory category,
            @RequestParam(required = false) TransportType transportType,
            @RequestParam(required = false) LostItemStatus status,
            @RequestParam(required = false) String location,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<LostItemResponse> response = lostItemService.getLostItems(keyword, category, transportType, status, location, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lost items retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lost item details by ID")
    public ResponseEntity<ApiResponse<LostItemResponse>> getLostItemById(@PathVariable Long id) {
        LostItemResponse response = lostItemService.getLostItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Lost item retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing lost item report (Ownership or Staff required)")
    public ResponseEntity<ApiResponse<LostItemResponse>> updateLostItem(
            @PathVariable Long id,
            @Valid @RequestBody LostItemUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        LostItemResponse response = lostItemService.updateLostItem(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lost item updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a lost item listing (Ownership or Staff required)")
    public ResponseEntity<ApiResponse<Void>> deleteLostItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        lostItemService.deleteLostItem(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lost item deleted successfully", null));
    }

    @GetMapping("/{id}/matches")
    @Operation(summary = "Get heuristic matches between this lost item and recorded found items")
    public ResponseEntity<ApiResponse<List<ItemMatchResponse>>> getMatchesForLostItem(@PathVariable Long id) {
        List<ItemMatchResponse> matches = matchService.getMatchesForLostItem(id);
        return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
    }
}
