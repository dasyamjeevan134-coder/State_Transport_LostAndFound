package com.transport.lostfound.service;

import com.transport.lostfound.dto.response.DashboardStatsResponse;
import com.transport.lostfound.enums.ClaimStatus;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.repository.ClaimRepository;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.LostItemRepository;
import com.transport.lostfound.repository.ReturnRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final ClaimRepository claimRepository;
    private final ReturnRecordRepository returnRecordRepository;

    public DashboardService(LostItemRepository lostItemRepository,
                            FoundItemRepository foundItemRepository,
                            ClaimRepository claimRepository,
                            ReturnRecordRepository returnRecordRepository) {
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.claimRepository = claimRepository;
        this.returnRecordRepository = returnRecordRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalLost = lostItemRepository.count();
        long openLost = lostItemRepository.countByStatus(LostItemStatus.OPEN);
        long totalFound = foundItemRepository.count();
        long availableFound = foundItemRepository.countByStatus(FoundItemStatus.AVAILABLE);
        long pendingClaims = claimRepository.countByClaimStatus(ClaimStatus.PENDING);
        long approvedClaims = claimRepository.countByClaimStatus(ClaimStatus.APPROVED);
        long totalReturns = returnRecordRepository.count();

        return new DashboardStatsResponse(
                totalLost,
                openLost,
                totalFound,
                availableFound,
                pendingClaims,
                approvedClaims,
                totalReturns
        );
    }
}
