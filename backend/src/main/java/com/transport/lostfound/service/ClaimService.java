package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.ClaimCreateRequest;
import com.transport.lostfound.dto.request.ClaimStatusUpdateRequest;
import com.transport.lostfound.dto.response.ClaimResponse;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.ClaimStatus;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.exception.BadRequestException;
import com.transport.lostfound.exception.ForbiddenException;
import com.transport.lostfound.exception.ResourceNotFoundException;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.UserRepository;
import com.transport.lostfound.security.UserPrincipal;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final FoundItemRepository foundItemRepository;
    private final UserRepository userRepository;

    public ClaimService(ClaimRepository claimRepository,
                        FoundItemRepository foundItemRepository,
                        UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.foundItemRepository = foundItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ClaimResponse submitClaim(ClaimCreateRequest request, Long passengerId) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", passengerId));

        FoundItem foundItem = foundItemRepository.findById(request.getFoundItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", request.getFoundItemId()));

        if (foundItem.getStatus() == FoundItemStatus.RETURNED || foundItem.getStatus() == FoundItemStatus.DISPOSED) {
            throw new BadRequestException("Item is no longer available for claims (status: " + foundItem.getStatus() + ")");
        }

        boolean activeClaimExists = claimRepository.existsByFoundItemIdAndPassengerIdAndClaimStatusIn(
                foundItem.getId(),
                passengerId,
                List.of(ClaimStatus.PENDING, ClaimStatus.APPROVED)
        );

        if (activeClaimExists) {
            throw new BadRequestException("You already have an active or approved claim for this item");
        }

        Claim claim = new Claim(
                foundItem,
                passenger,
                request.getOwnershipEvidence().trim(),
                ClaimStatus.PENDING
        );

        Claim savedClaim = claimRepository.save(claim);

        if (foundItem.getStatus() == FoundItemStatus.AVAILABLE) {
            foundItem.setStatus(FoundItemStatus.CLAIM_PENDING);
            foundItemRepository.save(foundItem);
        }

        return ClaimResponse.fromEntity(savedClaim);
    }

    @Transactional
    public ClaimResponse reviewClaim(Long claimId, ClaimStatusUpdateRequest request, Long reviewerId) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerId));

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        FoundItem foundItem = claim.getFoundItem();

        claim.setClaimStatus(request.getStatus());
        claim.setReviewer(reviewer);
        claim.setReviewNotes(request.getReviewNotes() != null ? request.getReviewNotes().trim() : null);
        claim.setReviewedAt(LocalDateTime.now());

        if (request.getStatus() == ClaimStatus.APPROVED) {
            if (foundItem.getStatus() == FoundItemStatus.RETURNED || foundItem.getStatus() == FoundItemStatus.DISPOSED) {
                throw new BadRequestException("Item is no longer available (status: " + foundItem.getStatus() + ")");
            }
            boolean hasOtherApproved = claimRepository.existsByFoundItemIdAndClaimStatus(foundItem.getId(), ClaimStatus.APPROVED);
            if (hasOtherApproved && claim.getClaimStatus() != ClaimStatus.APPROVED) {
                throw new BadRequestException("Another claim for this found item is already approved");
            }
            foundItem.setStatus(FoundItemStatus.CLAIM_APPROVED);
            foundItemRepository.save(foundItem);
        } else if (request.getStatus() == ClaimStatus.REJECTED || request.getStatus() == ClaimStatus.CANCELLED) {
            boolean hasOtherPending = claimRepository.existsByFoundItemIdAndClaimStatus(foundItem.getId(), ClaimStatus.PENDING);
            boolean hasOtherApproved = claimRepository.existsByFoundItemIdAndClaimStatus(foundItem.getId(), ClaimStatus.APPROVED);

            if (!hasOtherApproved && !hasOtherPending && foundItem.getStatus() != FoundItemStatus.RETURNED) {
                foundItem.setStatus(FoundItemStatus.AVAILABLE);
                foundItemRepository.save(foundItem);
            }
        }

        Claim updatedClaim = claimRepository.save(claim);
        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Transactional
    public ClaimResponse cancelClaim(Long claimId, Long passengerId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        if (!claim.getPassenger().getId().equals(passengerId)) {
            throw new ForbiddenException("You can only cancel your own claims");
        }

        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be cancelled");
        }

        claim.setClaimStatus(ClaimStatus.CANCELLED);
        Claim updatedClaim = claimRepository.save(claim);

        FoundItem foundItem = claim.getFoundItem();
        boolean hasOtherPending = claimRepository.existsByFoundItemIdAndClaimStatus(foundItem.getId(), ClaimStatus.PENDING);
        if (!hasOtherPending && foundItem.getStatus() == FoundItemStatus.CLAIM_PENDING) {
            foundItem.setStatus(FoundItemStatus.AVAILABLE);
            foundItemRepository.save(foundItem);
        }

        return ClaimResponse.fromEntity(updatedClaim);
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(Long claimId, UserPrincipal currentUser) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", claimId));

        boolean isPassenger = claim.getPassenger().getId().equals(currentUser.getId());
        boolean isStaffOrAdmin = currentUser.getRole() == Role.STAFF || currentUser.getRole() == Role.ADMIN;

        if (!isPassenger && !isStaffOrAdmin) {
            throw new ForbiddenException("You do not have permission to view this claim");
        }

        return ClaimResponse.fromEntity(claim);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClaimResponse> getMyClaims(Long passengerId, Pageable pageable) {
        Page<Claim> page = claimRepository.findByPassengerId(passengerId, pageable);
        List<ClaimResponse> content = page.getContent().stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsForFoundItem(Long foundItemId, UserPrincipal currentUser) {
        if (currentUser.getRole() != Role.STAFF && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only staff and administrators can view all claims on an item");
        }

        return claimRepository.findByFoundItemId(foundItemId).stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<ClaimResponse> getAllClaims(ClaimStatus status, Pageable pageable) {
        Specification<Claim> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("claimStatus"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Claim> page = claimRepository.findAll(spec, pageable);
        List<ClaimResponse> content = page.getContent().stream()
                .map(ClaimResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.of(page, content);
    }
}
