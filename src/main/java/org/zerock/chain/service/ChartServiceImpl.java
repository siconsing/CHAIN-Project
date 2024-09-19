package org.zerock.chain.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zerock.chain.dto.ChartDTO;
import org.zerock.chain.dto.ChartRequestDTO;
import org.zerock.chain.model.Chart;
import org.zerock.chain.repository.ChartRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChartServiceImpl extends BaseService<Chart> implements ChartService {

    private final ChartRepository chartRepository;
    private final ModelMapper modelMapper;

    @Override
    protected List<Chart> getAllItemsByEmpNo(Long empNo) {
        // Retrieve charts by employee number (chartAuthor)
        return chartRepository.findByChartAuthor(empNo);
    }

    @Override   // 차트 생성 등록
    public ChartDTO createChart(ChartRequestDTO chartRequestDTO) {
        // Get employee number from session
        Long empNo = getEmpNoFromSession();
        chartRequestDTO.setChartAuthor(empNo);

        Chart chart = modelMapper.map(chartRequestDTO, Chart.class);

        // Process progress labels 리스트로 라벨 저장
        String labelsString = String.join(",",
                chartRequestDTO.getProgressLabel20(),
                chartRequestDTO.getProgressLabel40(),
                chartRequestDTO.getProgressLabel60(),
                chartRequestDTO.getProgressLabel80(),
                chartRequestDTO.getProgressLabel100()
        );
        chart.setProgressLabels(labelsString);

        // chartProgress가 100이면 noticePinned를 true로 설정, 그렇지 않으면 false
        int chartProgress = Integer.parseInt(chartRequestDTO.getChartProgress());
        chart.setNoticePinned(chartProgress == 100);

        chart = chartRepository.save(chart);
        return modelMapper.map(chart, ChartDTO.class);
    }

    @Override   // 수정 등록
    public ChartDTO updateChart(Long chartNo, ChartRequestDTO chartRequestDTO) {
        Long empNo = getEmpNoFromSession();
        Chart chart = chartRepository.findByChartNoAndChartAuthor(chartNo, empNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chart ID or unauthorized access"));

        // 기존 chartAuthor 값을 유지
        chartRequestDTO.setChartAuthor(chart.getChartAuthor());

        modelMapper.map(chartRequestDTO, chart);

        // Process progress labels
        String labelsString = String.join(",",
                chartRequestDTO.getProgressLabel20(),
                chartRequestDTO.getProgressLabel40(),
                chartRequestDTO.getProgressLabel60(),
                chartRequestDTO.getProgressLabel80(),
                chartRequestDTO.getProgressLabel100()
        );
        chart.setProgressLabels(labelsString);

        // chartProgress가 100이면 noticePinned를 true로 설정, 그렇지 않으면 false
        int chartProgress = Integer.parseInt(chartRequestDTO.getChartProgress());
        chart.setNoticePinned(chartProgress == 100);

        chart = chartRepository.save(chart);
        return modelMapper.map(chart, ChartDTO.class);
    }


    @Override   // 전체 차트 조회
    public List<ChartDTO> getAllCharts() {
        Long empNo = getEmpNoFromSession();
        List<Chart> charts = getItemsByEmpNo(empNo, chartRepository::findByChartAuthor);
        return charts.stream()
                .map(chart -> modelMapper.map(chart, ChartDTO.class))
                .collect(Collectors.toList());
    }

    @Override   // 특정 차트 조회
    public ChartDTO getChartById(Long chartNo) {
        Long empNo = getEmpNoFromSession();
        Chart chart = chartRepository.findByChartNoAndChartAuthor(chartNo, empNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chart ID or unauthorized access"));
        return modelMapper.map(chart, ChartDTO.class);
    }

    @Override   // 차트 삭제
    public void deleteChart(Long chartNo) {
        Long empNo = getEmpNoFromSession();
        Chart chart = chartRepository.findByChartNoAndChartAuthor(chartNo, empNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chart ID or unauthorized access"));
        chartRepository.delete(chart);
    }

    // 차트 비교
    @Override
    public List<ChartDTO> getChartsByIds(List<Long> chartIds) {
        Long empNo = getEmpNoFromSession();
        List<Chart> charts = chartRepository.findAllById(chartIds).stream()
                .filter(chart -> chart.getChartAuthor().equals(empNo))
                .collect(Collectors.toList());

        return charts.stream()
                .map(chart -> modelMapper.map(chart, ChartDTO.class))
                .collect(Collectors.toList());
    }


    // 세션에서 직원 번호를 가져오는 메서드
    private Long getEmpNoFromSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        return (Long) session.getAttribute("empNo");
    }

    // 페이지네이션 관련
    @Override
    public Page<ChartDTO> getGoals(Pageable pageable) {
        return chartRepository.findAll(pageable).map(chart -> modelMapper.map(chart, ChartDTO.class));
    }
    @Override
    public int getTotalGoalCount() {
        return (int) chartRepository.count();
    }


}
