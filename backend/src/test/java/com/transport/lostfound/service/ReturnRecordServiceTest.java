package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.ReturnRecordCreateRequest;
import com.transport.lostfound.dto.response.ReturnRecordResponse;
import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.ReturnRecord;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.*;
import com.transport.lostfound.exception.BadRequestException;
import com.transport.lostfound.exception.DuplicateResourceException;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.ReturnRecordRepository;
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
class ReturnRecordServiceTest {

    @Mock
    private ReturnRecordRepository returnRecordRepository;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReturnRecordService returnRecordService;

    private User staff;
    private User passenger;
    private FoundItem foundItem;
    private Claim approvedClaim;
    private ReturnRecord sampleRecord;

    @BeforeEach
    void setUp() {
        staff = new User("Staff Sam", "sam@transport.gov", "hash", "+2222222", Role.STAFF);
        staff.setId(20L);

        passenger = new User("Passenger Pete", "pete@test.com", "hash", "+1111111", Role.PASSENGER);
        passenger.setId(10L);

        foundItem = new FoundItem(
                "Backpack",
                ItemCategory.BAGS_AND_LUGGAGE,
                "Blue backpack",
                LocalDateTime.now().minusDays(1),
                TransportType.TRAIN,
                "Route 10",
                "Grand Central",
                null,
                staff,
                FoundItemStatus.CLAIM_APPROVED
        );
        foundItem.setId(40L);

        approvedClaim = new Claim(foundItem, passenger, "Proof attached", ClaimStatus.APPROVED);
        approvedClaim.setId(50L);

        sampleRecord = new ReturnRecord(foundItem, approvedClaim, staff, LocalDateTime.now(), "Handed over with ID check");
        sampleRecord.setId(60L);
    }

    @Test
    void createReturnRecord_Success() {
        ReturnRecordCreateRequest request = new ReturnRecordCreateRequest(40L, 50L, LocalDateTime.now(), "Handed over with ID check");

        when(userRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(foundItemRepository.findById(40L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.findById(50L)).thenReturn(Optional.of(approvedClaim));
        when(returnRecordRepository.existsByFoundItemId(40L)).thenReturn(false);
        when(returnRecordRepository.existsByClaimId(50L)).thenReturn(false);
        when(returnRecordRepository.save(any(ReturnRecord.class))).thenReturn(sampleRecord);

        ReturnRecordResponse response = returnRecordService.createReturnRecord(request, 20L);

        assertNotNull(response);
        assertEquals(60L, response.getId());
        assertEquals(FoundItemStatus.RETURNED, foundItem.getStatus());
        verify(foundItemRepository).save(foundItem);
        verify(returnRecordRepository).save(any(ReturnRecord.class));
    }

    @Test
    void createReturnRecord_PendingClaim_ThrowsBadRequest() {
        approvedClaim.setClaimStatus(ClaimStatus.PENDING);
        ReturnRecordCreateRequest request = new ReturnRecordCreateRequest(40L, 50L, LocalDateTime.now(), "Premature handover");

        when(userRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(foundItemRepository.findById(40L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.findById(50L)).thenReturn(Optional.of(approvedClaim));

        assertThrows(BadRequestException.class, () -> returnRecordService.createReturnRecord(request, 20L));
    }

    @Test
    void createReturnRecord_AlreadyReturned_ThrowsDuplicate() {
        ReturnRecordCreateRequest request = new ReturnRecordCreateRequest(40L, 50L, LocalDateTime.now(), "Duplicate handover");

        when(userRepository.findById(20L)).thenReturn(Optional.of(staff));
        when(foundItemRepository.findById(40L)).thenReturn(Optional.of(foundItem));
        when(claimRepository.findById(50L)).thenReturn(Optional.of(approvedClaim));
        when(returnRecordRepository.existsByFoundItemId(40L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> returnRecordService.createReturnRecord(request, 20L));
    }
}
