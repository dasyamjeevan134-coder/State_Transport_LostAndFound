package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.ReturnRecordCreateRequest;
import com.transport.lostfound.dto.response.ApiResponse;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.dto.response.ReturnRecordResponse;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.ReturnRecordService;
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
@RequestMapping("/api/returns")
@Tag(name = "Handover & Returns", description = "Endpoints for formal return records and item handovers")
public class ReturnRecordController {

    private final ReturnRecordService returnRecordService;

    public ReturnRecordController(ReturnRecordService returnRecordService) {
        this.returnRecordService = returnRecordService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Complete item handover and record return (Staff/Admin only)")
    public ResponseEntity<ApiResponse<ReturnRecordResponse>> createReturnRecord(
            @Valid @RequestBody ReturnRecordCreateRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ReturnRecordResponse response = returnRecordService.createReturnRecord(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return record created and item marked as returned", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get all return records with pagination (Staff/Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<ReturnRecordResponse>>> getAllReturnRecords(
            @PageableDefault(sort = "returnDate", direction = Sort.Direction.DESC) Pageable pageable) {
        PagedResponse<ReturnRecordResponse> response = returnRecordService.getAllReturnRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success("Return records retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get return record by ID (Staff/Admin only)")
    public ResponseEntity<ApiResponse<ReturnRecordResponse>> getReturnRecordById(@PathVariable Long id) {
        ReturnRecordResponse response = returnRecordService.getReturnRecordById(id);
        return ResponseEntity.ok(ApiResponse.success("Return record retrieved successfully", response));
    }

    @GetMapping("/by-found-item/{foundItemId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get return record by found item ID (Staff/Admin only)")
    public ResponseEntity<ApiResponse<ReturnRecordResponse>> getReturnRecordByFoundItemId(
            @PathVariable Long foundItemId) {
        ReturnRecordResponse response = returnRecordService.getReturnRecordByFoundItemId(foundItemId);
        return ResponseEntity.ok(ApiResponse.success("Return record retrieved successfully", response));
    }

    @GetMapping("/by-claim/{claimId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get return record by claim ID (Staff/Admin only)")
    public ResponseEntity<ApiResponse<ReturnRecordResponse>> getReturnRecordByClaimId(
            @PathVariable Long claimId) {
        ReturnRecordResponse response = returnRecordService.getReturnRecordByClaimId(claimId);
        return ResponseEntity.ok(ApiResponse.success("Return record retrieved successfully", response));
    }
}
