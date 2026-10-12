package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.ClaimCreateRequest;
import com.transport.lostfound.dto.request.ClaimStatusUpdateRequest;
import com.transport.lostfound.dto.response.ApiResponse;
import com.transport.lostfound.dto.response.ClaimResponse;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.enums.ClaimStatus;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.ClaimService;
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

@RestController
@RequestMapping("/api/claims")
@Tag(name = "Item Claims", description = "Endpoints for claiming found items and staff approval workflow")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @Operation(summary = "Submit an ownership claim for an available found item")
    public ResponseEntity<ApiResponse<ClaimResponse>> submitClaim(
            @Valid @RequestBody ClaimCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.submitClaim(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Claim submitted successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim details by ID (Owner or Staff/Admin)")
    public ResponseEntity<ApiResponse<ClaimResponse>> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.getClaimById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Claim retrieved successfully", response));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Review ownership claim (Approve or Reject, Staff/Admin only)")
    public ResponseEntity<ApiResponse<ClaimResponse>> reviewClaim(
            @PathVariable Long id,
            @Valid @RequestBody ClaimStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.reviewClaim(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Claim status updated successfully", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel own pending claim (Passenger only)")
    public ResponseEntity<ApiResponse<ClaimResponse>> cancelClaim(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ClaimResponse response = claimService.cancelClaim(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Claim cancelled successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "List all claims with optional status filter and pagination (Staff/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<ClaimResponse>>> getAllClaims(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ClaimResponse> response = claimService.getAllClaims(status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Claims retrieved successfully", response));
    }
}
