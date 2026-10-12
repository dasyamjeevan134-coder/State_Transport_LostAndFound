package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.LostItemCreateRequest;
import com.transport.lostfound.dto.request.LostItemUpdateRequest;
import com.transport.lostfound.dto.response.LostItemResponse;
import com.transport.lostfound.dto.response.PagedResponse;
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
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LostItemService {

    private final LostItemRepository lostItemRepository;
    private final UserRepository userRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final ItemMatchService itemMatchService;

    public LostItemService(LostItemRepository lostItemRepository,
                           UserRepository userRepository,
                           ItemMatchRepository itemMatchRepository,
                           ItemMatchService itemMatchService) {
        this.lostItemRepository = lostItemRepository;
        this.userRepository = userRepository;
        this.itemMatchRepository = itemMatchRepository;
        this.itemMatchService = itemMatchService;
    }

    @Transactional
    public LostItemResponse createLostItem(LostItemCreateRequest request, Long passengerId) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", passengerId));

        LostItem lostItem = new LostItem(
                request.getItemName().trim(),
                request.getCategory(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getLossDateTime(),
                request.getTransportType(),
                request.getRouteNumber().trim(),
                request.getLocation().trim(),
                request.getPhotoUrl(),
                passenger,
                LostItemStatus.OPEN
        );

        LostItem savedItem = lostItemRepository.save(lostItem);

        // Run heuristic auto-matching asynchronously / transactionally
        try {
            itemMatchService.generateMatchesForLostItem(savedItem);
        } catch (Exception ignored) {
        }

        return LostItemResponse.fromEntity(savedItem);
    }

    @Transactional(readOnly = true)
    public LostItemResponse getLostItemById(Long id) {
        LostItem lostItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));
        return LostItemResponse.fromEntity(lostItem);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LostItemResponse> getLostItems(String keyword,
                                                        ItemCategory category,
                                                        TransportType transportType,
                                                        LostItemStatus status,
                                                        String location,
                                                        Pageable pageable) {
        Specification<LostItem> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("itemName")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (transportType != null) {
                predicates.add(cb.equal(root.get("transportType"), transportType));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase().trim() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LostItem> page = lostItemRepository.findAll(spec, pageable);
        List<LostItemResponse> content = page.getContent().stream()
                .map(LostItemResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(page, content);
    }

    @Transactional
    public LostItemResponse updateLostItem(Long id, LostItemUpdateRequest request, UserPrincipal currentUser) {
        LostItem lostItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));

        validateOwnershipOrStaff(lostItem.getPassenger().getId(), currentUser, "update");

        if (StringUtils.hasText(request.getItemName())) {
            lostItem.setItemName(request.getItemName().trim());
        }
        if (request.getCategory() != null) {
            lostItem.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            lostItem.setDescription(request.getDescription().trim());
        }
        if (request.getLossDateTime() != null) {
            lostItem.setLossDateTime(request.getLossDateTime());
        }
        if (request.getTransportType() != null) {
            lostItem.setTransportType(request.getTransportType());
        }
        if (StringUtils.hasText(request.getRouteNumber())) {
            lostItem.setRouteNumber(request.getRouteNumber().trim());
        }
        if (StringUtils.hasText(request.getLocation())) {
            lostItem.setLocation(request.getLocation().trim());
        }
        if (request.getPhotoUrl() != null) {
            lostItem.setPhotoUrl(request.getPhotoUrl().trim());
        }
        if (request.getStatus() != null) {
            lostItem.setStatus(request.getStatus());
        }

        LostItem updatedItem = lostItemRepository.save(lostItem);

        try {
            itemMatchService.generateMatchesForLostItem(updatedItem);
        } catch (Exception ignored) {
        }

        return LostItemResponse.fromEntity(updatedItem);
    }

    @Transactional
    public void deleteLostItem(Long id, UserPrincipal currentUser) {
        LostItem lostItem = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LostItem", "id", id));

        validateOwnershipOrStaff(lostItem.getPassenger().getId(), currentUser, "delete");

        itemMatchRepository.deleteByLostItemId(id);
        lostItemRepository.delete(lostItem);
    }

    @Transactional(readOnly = true)
    public PagedResponse<LostItemResponse> getMyLostItems(Long passengerId, Pageable pageable) {
        Page<LostItem> page = lostItemRepository.findByPassengerId(passengerId, pageable);
        List<LostItemResponse> content = page.getContent().stream()
                .map(LostItemResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    private void validateOwnershipOrStaff(Long resourceOwnerId, UserPrincipal currentUser, String action) {
        boolean isOwner = resourceOwnerId.equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isStaffOrAdmin) {
            throw new ForbiddenException("You do not have permission to " + action + " this listing");
        }
    }
}
