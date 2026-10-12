package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.LostItemCreateRequest;
import com.transport.lostfound.dto.request.LostItemUpdateRequest;
import com.transport.lostfound.dto.response.LostItemResponse;
import com.transport.lostfound.entity.LostItem;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.exception.ForbiddenException;
import com.transport.lostfound.exception.ResourceNotFoundException;
import com.transport.lostfound.repository.ItemMatchRepository;
import com.transport.lostfound.repository.LostItemRepository;
import com.transport.lostfound.repository.UserRepository;
import com.transport.lostfound.security.UserPrincipal;
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
class LostItemServiceTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMatchRepository itemMatchRepository;

    @Mock
    private ItemMatchService itemMatchService;

    @InjectMocks
    private LostItemService lostItemService;

    private User passenger;
    private LostItem sampleItem;

    @BeforeEach
    void setUp() {
        passenger = new User("Alice Passenger", "alice@test.com", "hash", "+1234567", Role.PASSENGER);
        passenger.setId(10L);

        sampleItem = new LostItem(
                "Black Leather Wallet",
                ItemCategory.WALLETS_AND_PURSES,
                "Contains cards and ID",
                LocalDateTime.now().minusHours(2),
                TransportType.METRO,
                "Blue Line",
                "Central Station",
                null,
                passenger,
                LostItemStatus.OPEN
        );
        sampleItem.setId(100L);
    }

    @Test
    void createLostItem_Success() {
        LostItemCreateRequest request = new LostItemCreateRequest(
                "Black Leather Wallet",
                ItemCategory.WALLETS_AND_PURSES,
                "Contains cards and ID",
                LocalDateTime.now().minusHours(2),
                TransportType.METRO,
                "Blue Line",
                "Central Station",
                null
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(passenger));
        when(lostItemRepository.save(any(LostItem.class))).thenReturn(sampleItem);

        LostItemResponse response = lostItemService.createLostItem(request, 10L);

        assertNotNull(response);
        assertEquals("Black Leather Wallet", response.getItemName());
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getPassengerId());
        verify(lostItemRepository).save(any(LostItem.class));
    }

    @Test
    void updateLostItem_Owner_Success() {
        UserPrincipal ownerPrincipal = UserPrincipal.create(passenger);
        LostItemUpdateRequest updateRequest = new LostItemUpdateRequest();
        updateRequest.setItemName("Updated Wallet Name");

        when(lostItemRepository.findById(100L)).thenReturn(Optional.of(sampleItem));
        when(lostItemRepository.save(any(LostItem.class))).thenReturn(sampleItem);

        LostItemResponse response = lostItemService.updateLostItem(100L, updateRequest, ownerPrincipal);

        assertNotNull(response);
        assertEquals("Updated Wallet Name", sampleItem.getItemName());
    }

    @Test
    void updateLostItem_UnauthorizedUser_ThrowsForbidden() {
        User otherUser = new User("Bob Passenger", "bob@test.com", "hash", "+99999", Role.PASSENGER);
        otherUser.setId(20L);
        UserPrincipal otherPrincipal = UserPrincipal.create(otherUser);

        LostItemUpdateRequest updateRequest = new LostItemUpdateRequest();
        updateRequest.setItemName("Malicious Update");

        when(lostItemRepository.findById(100L)).thenReturn(Optional.of(sampleItem));

        assertThrows(ForbiddenException.class, () -> lostItemService.updateLostItem(100L, updateRequest, otherPrincipal));
        verify(lostItemRepository, never()).save(any(LostItem.class));
    }

    @Test
    void getLostItemById_NotFound_ThrowsException() {
        when(lostItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lostItemService.getLostItemById(999L));
    }

    @Test
    void deleteLostItem_Owner_Success() {
        UserPrincipal ownerPrincipal = UserPrincipal.create(passenger);
        when(lostItemRepository.findById(100L)).thenReturn(Optional.of(sampleItem));

        lostItemService.deleteLostItem(100L, ownerPrincipal);

        verify(itemMatchRepository).deleteByLostItemId(100L);
        verify(lostItemRepository).delete(sampleItem);
    }
}
