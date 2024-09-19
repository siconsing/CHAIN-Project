package org.zerock.chain.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.chain.dto.RankDTO;
import org.zerock.chain.repository.RankRepository;

import java.util.List;
import java.util.stream.Collectors;

// RankController.java
@RestController
@RequestMapping("/api/ranks")
public class RankController {

    private final RankRepository rankRepository;

    public RankController(RankRepository rankRepository) {
        this.rankRepository = rankRepository;
    }

    @GetMapping
    public ResponseEntity<List<RankDTO>> getAllRanks() {
        List<RankDTO> rankDTOs = rankRepository.findAll().stream()
                .map(rank -> {
                    RankDTO dto = new RankDTO();
                    dto.setRankNo(rank.getRankNo());
                    dto.setRankName(rank.getRankName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(rankDTOs);
    }
}

