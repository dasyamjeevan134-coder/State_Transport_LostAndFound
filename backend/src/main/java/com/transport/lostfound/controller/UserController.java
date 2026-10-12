package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.UserProfileUpdateRequest;
import com.transport.lostfound.dto.response.*;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.AuthService;
import com.transport.lostfound.service.ClaimService;
import com.transport.lostfound.service.FoundItemService;
import com.transport.lostfound.service.LostItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile & Dashboard", description = "Endpoints for viewing and updating personal profile and user submissions")
public class UserController {

    private final AuthService authService;
    private final LostItemService lostItemService;
    private final ClaimService claimService;
    private final FoundItemService foundItemService;

    public UserController(AuthService authService,
                          LostItemService lostItemService,
                          ClaimService claimService,
                          FoundItemService foundItemService) {
        this.authService = authService;
        this.lostItemService = lostItemService;
        this.claimService = claimService;
        this.foundItemService = foundItemService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse response = authService.getProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update personal profile information (name, phone, password)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = authService.updateProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @GetMapping("/my-lost-items")
    @Operation(summary = "Get lost items reported by current authenticated passenger")
    public ResponseEntity<ApiResponse<PagedResponse<LostItemResponse>>> getMyLostItems(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<LostItemResponse> response = lostItemService.getMyLostItems(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My lost items retrieved successfully", response));
    }

    @GetMapping("/my-claims")
    @Operation(summary = "Get claims submitted by current authenticated passenger")
    public ResponseEntity<ApiResponse<PagedResponse<ClaimResponse>>> getMyClaims(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ClaimResponse> response = claimService.getMyClaims(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My claims retrieved successfully", response));
    }

    @GetMapping("/my-found-items")
    @Operation(summary = "Get found items registered by current authenticated staff member")
    public ResponseEntity<ApiResponse<PagedResponse<FoundItemResponse>>> getMyFoundItems(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<FoundItemResponse> response = foundItemService.getMyReportedFoundItems(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("My reported found items retrieved successfully", response));
    }
}
