package com.transport.lostfound.repository;

import com.transport.lostfound.entity.ItemMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemMatchRepository extends JpaRepository<ItemMatch, Long> {

    List<ItemMatch> findByLostItemIdOrderByMatchScoreDesc(Long lostItemId);

    List<ItemMatch> findByFoundItemIdOrderByMatchScoreDesc(Long foundItemId);

    Optional<ItemMatch> findByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);

    @Modifying
    @Query("DELETE FROM ItemMatch m WHERE m.lostItem.id = :lostItemId")
    void deleteByLostItemId(@Param("lostItemId") Long lostItemId);

    @Modifying
    @Query("DELETE FROM ItemMatch m WHERE m.foundItem.id = :foundItemId")
    void deleteByFoundItemId(@Param("foundItemId") Long foundItemId);
}
