package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.ClaimCreateRequest;
import com.transport.lostfound.dto.request.ClaimStatusUpdateRequest;
import com.transport.lostfound.dto.response.ClaimResponse;
import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.*;
import com.transport.lostfound.exception.BadRequestException;
import com.transport.lostfound.exception.ForbiddenException;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User passenger;
    private User staff;
    private FoundItem foundItem;
    private Claim sampleClaim;

    @BeforeEach
    void setUp() {
        passenger = new User("Passenger Pete", "pete@test.com", "hash", "+1111111", Role.PASSENGER);
        passenger.setId(10L);

        staff = new User("Staff Sam", "sam@transport.gov", "hash", "+2222222", Role.STAFF);
        staff.setId(20L);

        foundItem = new FoundItem(
                "iPhone 14 Pro",
                ItemCategory.ELECTRONICS,
                "Space black with blue case",
                LocalDateTime.now().minusHours(1),
                TransportType.BUS,
                "Route 42",
                "Downtown Terminal",
                null,
                staff,
                FoundItemStatus.AVAILABLE
        );
        foundItem.setId(200L);

        sampleClaim = new Claim(foundItem, passenger, "I have the purchase invoice and IMEI number", ClaimStatus.PENDING);
        sampleClaim.setId(300L);
    }

    @Test
    void submitClaim_Success() {
        ClaimCreateRequest request = new ClaimCreateRequest(200L, "I have the purchase invoice and IMEI number");

        when(userRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(foundItemRepository.findById(200L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.existsByFoundItemIdAndPassengerIdAndClaimStatusIn(anyLong(), anyLong(), any())).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);

        ClaimResponse response = claimService.submitClaim(request, 10L);

        assertNotNull(response);
        assertEquals(300L, response.getId());
        assertEquals(ClaimStatus.PENDING, response.getClaimStatus());
        assertEquals(FoundItemStatus.CLAIM_PENDING, foundItem.getStatus());
        verify(foundItemRepository).save(foundItem);
    }

    @Test
    void submitClaim_ItemAlreadyReturned_ThrowsException() {
        foundItem.setStatus(FoundItemStatus.RETURNED);
        ClaimCreateRequest request = new ClaimCreateRequest(200L, "I have the purchase invoice and IMEI number");

        when(userRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(foundItemRepository.findById(200L)).thenReturn(Optional.of(foundItem));

        assertThrows(BadRequestException.class, () -> claimService.submitClaim(request, 10L));
    }

    @Test
    void submitClaim_DuplicateClaim_ThrowsException() {
        ClaimCreateRequest request = new ClaimCreateRequest(200L, "I have the purchase invoice and IMEI number");

        when(userRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(foundItemRepository.findById(200L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.existsByFoundItemIdAndPassengerIdAndClaimStatusIn(anyLong(), anyLong(), any())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> claimService.submitClaim(request, 10L));
    }

    @Test
    void reviewClaim_Approved_UpdatesStatuses() {
        ClaimStatusUpdateRequest request = new ClaimStatusUpdateRequest(ClaimStatus.APPROVED, "Verified IMEI match");

        when(userRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(claimRepository.findById(300L)).thenReturn(Optional.of(sampleClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);

        ClaimResponse response = claimService.reviewClaim(300L, request, 20L);

        assertNotNull(response);
        assertEquals(ClaimStatus.APPROVED, sampleClaim.getClaimStatus());
        assertEquals(FoundItemStatus.CLAIM_APPROVED, foundItem.getStatus());
        verify(foundItemRepository).save(foundItem);
    }

    @Test
    void reviewClaim_Rejected_RevertsFoundItemToAvailable() {
        ClaimStatusUpdateRequest request = new ClaimStatusUpdateRequest(ClaimStatus.REJECTED, "Invalid documents");

        when(userRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(claimRepository.findById(300L)).thenReturn(Optional.of(sampleClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);
        when(claimRepository.existsByFoundItemIdAndClaimStatus(200L, ClaimStatus.PENDING)).thenReturn(false);
        when(claimRepository.existsByFoundItemIdAndClaimStatus(200L, ClaimStatus.APPROVED)).thenReturn(false);

        ClaimResponse response = claimService.reviewClaim(300L, request, 20L);

        assertNotNull(response);
        assertEquals(ClaimStatus.REJECTED, sampleClaim.getClaimStatus());
        assertEquals(FoundItemStatus.AVAILABLE, foundItem.getStatus());
    }

    @Test
    void cancelClaim_Owner_Success() {
        when(claimRepository.findById(300L)).thenReturn(Optional.of(sampleClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(sampleClaim);

        ClaimResponse response = claimService.cancelClaim(300L, 10L);

        assertNotNull(response);
        assertEquals(ClaimStatus.CANCELLED, sampleClaim.getClaimStatus());
    }

    @Test
    void cancelClaim_NotOwner_ThrowsForbidden() {
        when(claimRepository.findById(300L)).thenReturn(Optional.of(sampleClaim));

        assertThrows(ForbiddenException.class, () -> claimService.cancelClaim(300L, 999L));
    }
}
