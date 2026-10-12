package com.transport.lostfound.service;

import com.transport.lostfound.dto.response.ItemMatchResponse;
import com.transport.lostfound.entity.FoundItem;
import com.transport.lostfound.entity.ItemMatch;
import com.transport.lostfound.entity.LostItem;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.LostItemStatus;
import com.transport.lostfound.repository.FoundItemRepository;
import com.transport.lostfound.repository.ItemMatchRepository;
import com.transport.lostfound.repository.LostItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemMatchService {

    private static final Logger logger = LoggerFactory.getLogger(ItemMatchService.class);

    private final ItemMatchRepository matchRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    public ItemMatchService(ItemMatchRepository matchRepository,
                            LostItemRepository lostItemRepository,
                            FoundItemRepository foundItemRepository) {
        this.matchRepository = matchRepository;
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
    }

    @Transactional
    public void generateMatchesForLostItem(LostItem lostItem) {
        if (lostItem == null || lostItem.getStatus() == LostItemStatus.RETURNED || lostItem.getStatus() == LostItemStatus.CLOSED) {
            return;
        }

        List<FoundItem> candidateFoundItems = foundItemRepository.findByStatus(FoundItemStatus.AVAILABLE);
        candidateFoundItems.addAll(foundItemRepository.findByStatus(FoundItemStatus.CLAIM_PENDING));

        boolean foundHighMatch = false;

        for (FoundItem foundItem : candidateFoundItems) {
            MatchCalculation calculation = calculateScore(lostItem, foundItem);
            if (calculation.score.compareTo(BigDecimal.valueOf(40.0)) >= 0) {
                saveOrUpdateMatch(lostItem, foundItem, calculation.score, calculation.notes);
                if (calculation.score.compareTo(BigDecimal.valueOf(60.0)) >= 0) {
                    foundHighMatch = true;
                }
            }
        }

        if (foundHighMatch && lostItem.getStatus() == LostItemStatus.OPEN) {
            lostItem.setStatus(LostItemStatus.MATCH_FOUND);
            lostItemRepository.save(lostItem);
        }
    }

    @Transactional
    public void generateMatchesForFoundItem(FoundItem foundItem) {
        if (foundItem == null || foundItem.getStatus() == FoundItemStatus.RETURNED || foundItem.getStatus() == FoundItemStatus.DISPOSED) {
            return;
        }

        List<LostItem> candidateLostItems = lostItemRepository.findByStatus(LostItemStatus.OPEN);
        candidateLostItems.addAll(lostItemRepository.findByStatus(LostItemStatus.MATCH_FOUND));

        for (LostItem lostItem : candidateLostItems) {
            MatchCalculation calculation = calculateScore(lostItem, foundItem);
            if (calculation.score.compareTo(BigDecimal.valueOf(40.0)) >= 0) {
                saveOrUpdateMatch(lostItem, foundItem, calculation.score, calculation.notes);
                if (calculation.score.compareTo(BigDecimal.valueOf(60.0)) >= 0 && lostItem.getStatus() == LostItemStatus.OPEN) {
                    lostItem.setStatus(LostItemStatus.MATCH_FOUND);
                    lostItemRepository.save(lostItem);
                }
            }
        }
    }

    private void saveOrUpdateMatch(LostItem lostItem, FoundItem foundItem, BigDecimal score, String notes) {
        Optional<ItemMatch> existing = matchRepository.findByLostItemIdAndFoundItemId(lostItem.getId(), foundItem.getId());
        if (existing.isPresent()) {
            ItemMatch match = existing.get();
            match.setMatchScore(score);
            match.setMatchNotes(notes);
            matchRepository.save(match);
        } else {
            ItemMatch match = new ItemMatch(lostItem, foundItem, score, notes);
            matchRepository.save(match);
        }
    }

    @Transactional(readOnly = true)
    public List<ItemMatchResponse> getMatchesForLostItem(Long lostItemId) {
        return matchRepository.findByLostItemIdOrderByMatchScoreDesc(lostItemId).stream()
                .map(ItemMatchResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ItemMatchResponse> getMatchesForFoundItem(Long foundItemId) {
        return matchRepository.findByFoundItemIdOrderByMatchScoreDesc(foundItemId).stream()
                .map(ItemMatchResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private MatchCalculation calculateScore(LostItem lost, FoundItem found) {
        double score = 0.0;
        List<String> matchedFeatures = new ArrayList<>();

        // 1. Category match (35 pts)
        if (lost.getCategory() == found.getCategory()) {
            score += 35.0;
            matchedFeatures.add("Same category (" + lost.getCategory() + ")");
        }

        // 2. Transport type match (15 pts)
        if (lost.getTransportType() == found.getTransportType()) {
            score += 15.0;
            matchedFeatures.add("Same transit mode (" + lost.getTransportType() + ")");
        }

        // 3. Route number match (15 pts)
        if (normalize(lost.getRouteNumber()).equalsIgnoreCase(normalize(found.getRouteNumber()))) {
            score += 15.0;
            matchedFeatures.add("Matching route (" + lost.getRouteNumber() + ")");
        }

        // 4. Location match (15 pts)
        String lostLoc = normalize(lost.getLocation());
        String foundLoc = normalize(found.getLocation());
        if (!lostLoc.isEmpty() && !foundLoc.isEmpty() && (lostLoc.equalsIgnoreCase(foundLoc) || lostLoc.contains(foundLoc) || foundLoc.contains(lostLoc))) {
            score += 15.0;
            matchedFeatures.add("Location overlap");
        }

        // 5. Date and time proximity (10 pts)
        if (lost.getLossDateTime() != null && found.getFoundDateTime() != null) {
            Duration diff = Duration.between(lost.getLossDateTime(), found.getFoundDateTime()).abs();
            long hours = diff.toHours();
            if (hours <= 48) {
                score += 10.0;
                matchedFeatures.add("Date within 48 hours");
            } else if (hours <= 168) { // 7 days
                score += 5.0;
                matchedFeatures.add("Date within 7 days");
            }
        }

        // 6. Item name similarity (10 pts)
        if (hasWordOverlap(lost.getItemName(), found.getItemName())) {
            score += 10.0;
            matchedFeatures.add("Item name keyword match");
        }

        BigDecimal finalScore = BigDecimal.valueOf(Math.min(100.0, score)).setScale(2, RoundingMode.HALF_UP);
        String notes = matchedFeatures.isEmpty() ? "Low correlation" : String.join(", ", matchedFeatures);

        return new MatchCalculation(finalScore, notes);
    }

    private String normalize(String str) {
        return str == null ? "" : str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    private boolean hasWordOverlap(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        Set<String> words1 = Arrays.stream(s1.toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
        Set<String> words2 = Arrays.stream(s2.toLowerCase().split("\\s+"))
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
        words1.retainAll(words2);
        return !words1.isEmpty();
    }

    private static class MatchCalculation {
        final BigDecimal score;
        final String notes;

        MatchCalculation(BigDecimal score, String notes) {
            this.score = score;
            this.notes = notes;
        }
    }
}
