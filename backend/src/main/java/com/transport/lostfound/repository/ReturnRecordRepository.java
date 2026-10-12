package com.transport.lostfound.repository;

import com.transport.lostfound.entity.ReturnRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {

    boolean existsByFoundItemId(Long foundItemId);

    boolean existsByClaimId(Long claimId);

    Optional<ReturnRecord> findByFoundItemId(Long foundItemId);

    Optional<ReturnRecord> findByClaimId(Long claimId);

    Page<ReturnRecord> findByStaffId(Long staffId, Pageable pageable);
}
