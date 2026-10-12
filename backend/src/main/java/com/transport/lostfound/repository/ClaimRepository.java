package com.transport.lostfound.repository;

import com.transport.lostfound.entity.Claim;
import com.transport.lostfound.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {

    long countByClaimStatus(ClaimStatus status);

    Page<Claim> findByPassengerId(Long passengerId, Pageable pageable);

    List<Claim> findByFoundItemId(Long foundItemId);

    boolean existsByFoundItemIdAndPassengerIdAndClaimStatusIn(Long foundItemId, Long passengerId, Collection<ClaimStatus> statuses);

    boolean existsByFoundItemIdAndClaimStatus(Long foundItemId, ClaimStatus claimStatus);
}
