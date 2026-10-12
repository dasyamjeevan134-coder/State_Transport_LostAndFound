package com.transport.lostfound.repository;

import com.transport.lostfound.entity.LostItem;
import com.transport.lostfound.enums.LostItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LostItemRepository extends JpaRepository<LostItem, Long>, JpaSpecificationExecutor<LostItem> {

    long countByStatus(LostItemStatus status);

    Page<LostItem> findByPassengerId(Long passengerId, Pageable pageable);

    List<LostItem> findByStatus(LostItemStatus status);
}
