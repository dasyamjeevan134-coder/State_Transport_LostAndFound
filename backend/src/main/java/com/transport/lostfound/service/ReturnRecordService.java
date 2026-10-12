package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.ReturnRecordCreateRequest;
import com.transport.lostfound.dto.response.PagedResponse;
import com.transport.lostfound.dto.response.ReturnRecordResponse;
import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.ReturnRecord;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.ClaimStatus;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.exception.BadRequestException;
import com.transport.lostfound.exception.DuplicateResourceException;
import com.transport.lostfound.exception.ResourceNotFoundException;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.ReturnRecordRepository;
import com.transport.lostfound.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReturnRecordService {

    private final ReturnRecordRepository returnRecordRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    public ReturnRecordService(ReturnRecordRepository returnRecordRepository,
                               FoundItemRepository foundItemRepository,
                               ClaimRepository claimRepository,
                               UserRepository userRepository) {
        this.returnRecordRepository = returnRecordRepository;
        this.foundItemRepository = foundItemRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReturnRecordResponse createReturnRecord(ReturnRecordCreateRequest request, Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", staffId));

        FoundItem foundItem = foundItemRepository.findById(request.getFoundItemId())
                .orElseThrow(() -> new ResourceNotFoundException("FoundItem", "id", request.getFoundItemId()));

        Claim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new ResourceNotFoundException("Claim", "id", request.getClaimId()));

        if (!claim.getFoundItem().getId().equals(foundItem.getId())) {
            throw new BadRequestException("The specified claim does not belong to this found item");
        }

        if (claim.getClaimStatus() != ClaimStatus.APPROVED) {
            throw new BadRequestException("A return record can only be created for an APPROVED claim");
        }

        if (returnRecordRepository.existsByFoundItemId(foundItem.getId())) {
            throw new DuplicateResourceException("A return record already exists for this found item");
        }

        if (returnRecordRepository.existsByClaimId(claim.getId())) {
            throw new DuplicateResourceException("A return record already exists for this claim");
        }

        LocalDateTime returnDate = request.getReturnDate() != null ? request.getReturnDate() : LocalDateTime.now();

        ReturnRecord returnRecord = new ReturnRecord(
                foundItem,
                claim,
                staff,
                returnDate,
                request.getRemarks() != null ? request.getRemarks().trim() : null
        );

        ReturnRecord saved = returnRecordRepository.save(returnRecord);

        foundItem.setStatus(FoundItemStatus.RETURNED);
        foundItemRepository.save(foundItem);

        return ReturnRecordResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ReturnRecordResponse getReturnRecordById(Long id) {
        ReturnRecord record = returnRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRecord", "id", id));
        return ReturnRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public ReturnRecordResponse getReturnRecordByFoundItemId(Long foundItemId) {
        ReturnRecord record = returnRecordRepository.findByFoundItemId(foundItemId)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRecord", "foundItemId", foundItemId));
        return ReturnRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public ReturnRecordResponse getReturnRecordByClaimId(Long claimId) {
        ReturnRecord record = returnRecordRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("ReturnRecord", "claimId", claimId));
        return ReturnRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ReturnRecordResponse> getAllReturnRecords(Pageable pageable) {
        Page<ReturnRecord> page = returnRecordRepository.findAll(pageable);
        List<ReturnRecordResponse> content = page.getContent().stream()
                .map(ReturnRecordResponse::fromEntity)
                .collect(Collectors.toList());
        return PagedResponse.of(page, content);
    }
}
