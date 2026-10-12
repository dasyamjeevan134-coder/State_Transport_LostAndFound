package com.transport.lostfound.repository;

import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.enums.FoundItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long>, JpaSpecificationExecutor<FoundItem> {

    long countByStatus(FoundItemStatus status);

    Page<FoundItem> findByStaffId(Long staffId, Pageable pageable);

    List<FoundItem> findByStatus(FoundItemStatus status);
}
