package org.zerock.chain.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zerock.chain.dto.ChartDTO;
import org.zerock.chain.dto.ChartRequestDTO;

import java.util.List;

public interface ChartService {

    List<ChartDTO> getAllCharts();    // 전체 목록 조회
    ChartDTO getChartById(Long chartNo);    // 개별 상세 조회
    ChartDTO createChart(ChartRequestDTO chartRequestDTO);    // 생성
    ChartDTO updateChart(Long chartNo, ChartRequestDTO chartRequestDTO);    // 수정
    void deleteChart(Long chartNo);    // 삭제
    List<ChartDTO> getChartsByIds(List<Long> chartIds); // 차트 비교

    // 페이지네이션
    Page<ChartDTO> getGoals(Pageable pageable);
    int getTotalGoalCount();
}
