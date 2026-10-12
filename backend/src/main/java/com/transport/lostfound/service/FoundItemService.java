package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.FoundItemCreateRequest;
import com.transport.lostfound.dto.request.FoundItemUpdateRequest;
import com.transport.lostfound.dto.response.FoundItemResponse;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.exception.ForbiddenException;
import com.transport.lostfound.exception.ResourceNotFoundException;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.ItemMatchRepository;
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
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;
    private final ItemMatchRepository itemMatchRepository;
    private final ItemMatchService itemMatchService;

    public FoundItemService(FoundItemRepository foundItemRepository,
                            UserRepository userRepository,
                            ItemMatchRepository itemMatchRepository,
                            ItemMatchService itemMatchService) {
        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
        this.itemMatchRepository = itemMatchRepository;
        this.itemMatchService = itemMatchService;
    }

    @Transactional
    public FoundItemResponse createFoundItem(FoundItemCreateRequest request, Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", staffId));

        FoundItem foundItem = new FoundItem(
                request.getItemName().trim(),
                request.getCategory(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getFoundDateTime(),
                request.getTransportType(),
                request.getRouteNumber().trim(),
                request.getLocation().trim(),
                request.getPhotoUrl(),
                staff,
                FoundItemStatus.AVAILABLE
        );

        FoundItem savedItem = foundItemRepository.save(foundItem);

        try {
            itemMatchService.generateMatchesForFoundItem(savedItem);
        } catch (Exception ignored) {
        }

        return FoundItemResponse.fromEntity(savedItem);
    }

    @Transactional(readOnly = true)
    public FoundItemResponse getFoundItemById(Long id) {
        FoundItem foundItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));
        return FoundItemResponse.fromEntity(foundItem);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FoundItemResponse> getFoundItems(String keyword,
                                                          ItemCategory category,
                                                          TransportType transportType,
                                                          FoundItemStatus status,
                                                          String location,
                                                          Pageable pageable) {
        Specification<FoundItem> spec = (root, query, cb) -> {
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

        Page<FoundItem> page = foundItemRepository.findAll(spec, pageable);
        List<FoundItemResponse> content = page.getContent().stream()
                .map(FoundItemResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(page, content);
    }

    @Transactional
    public FoundItemResponse updateFoundItem(Long id, FoundItemUpdateRequest request, UserPrincipal currentUser) {
        FoundItem foundItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));

        validateStaffOrAdmin(currentUser);

        if (StringUtils.hasText(request.getItemName())) {
            foundItem.setItemName(request.getItemName().trim());
        }
        if (request.getCategory() != null) {
            foundItem.setCategory(request.getCategory());
        }
        if (request.getDescription() != null) {
            foundItem.setDescription(request.getDescription().trim());
        }
        if (request.getFoundDateTime() != null) {
            foundItem.setFoundDateTime(request.getFoundDateTime());
        }
        if (request.getTransportType() != null) {
            foundItem.setTransportType(request.getTransportType());
        }
        if (StringUtils.hasText(request.getRouteNumber())) {
            foundItem.setRouteNumber(request.getRouteNumber().trim());
        }
        if (StringUtils.hasText(request.getLocation())) {
            foundItem.setLocation(request.getLocation().trim());
        }
        if (request.getPhotoUrl() != null) {
            foundItem.setPhotoUrl(request.getPhotoUrl().trim());
        }
        if (request.getStatus() != null) {
            foundItem.setStatus(request.getStatus());
        }

        FoundItem updatedItem = foundItemRepository.save(foundItem);

        try {
            itemMatchService.generateMatchesForFoundItem(updatedItem);
        } catch (Exception ignored) {
        }

        return FoundItemResponse.fromEntity(updatedItem);
    }

    @Transactional
    public void deleteFoundItem(Long id, UserPrincipal currentUser) {
        FoundItem foundItem = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", id));

        validateStaffOrAdmin(currentUser);

        itemMatchRepository.deleteByFoundItemId(id);
        foundItemRepository.delete(foundItem);
    }

    @Transactional(readOnly = true)
    public PagedResponse<FoundItemResponse> getMyReportedFoundItems(Long staffId, Pageable pageable) {
        Page<FoundItem> page = foundItemRepository.findByStaffId(staffId, pageable);
        List<FoundItemResponse> content = page.getContent().stream()
                .map(FoundItemResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    private void validateStaffOrAdmin(UserPrincipal currentUser) {
        if (currentUser.getRole() != Role.STAFF && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only transit staff or administrators can modify found item listings");
        }
    }
}
