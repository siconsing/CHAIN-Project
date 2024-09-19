package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.ChartDTO;
import org.zerock.chain.dto.ChartRequestDTO;
import org.zerock.chain.dto.ProjectDTO;
import org.zerock.chain.service.ChartService;
import org.zerock.chain.service.ProjectService;

import java.util.*;

@Controller
@RequestMapping("/chart")
@Log4j2
public class ChartController {

    private final ChartService chartService;  // 차트 서비스 의존성 주입
    private final ModelMapper modelMapper;  // ModelMapper 의존성 주입
    private final ProjectService projectService;  // 프로젝트 서비스 의존성 주입

    @Autowired
    public ChartController(ChartService chartService, ModelMapper modelMapper, ProjectService projectService) {
        this.chartService = chartService;  // ChartService 초기화
        this.modelMapper = modelMapper;  // ModelMapper 초기화
        this.projectService = projectService;  // ProjectService 초기화
    }

    // 차트 데이터를 JSON으로 반환하는 메서드
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChartData() {
        // 차트 데이터를 가져옴
        List<ChartDTO> charts = chartService.getAllCharts();

        // 프로젝트 데이터를 가져옴
        List<ProjectDTO> projects = projectService.getAllProjects();

        // 데이터를 Map에 담아서 반환
        Map<String, Object> response = new HashMap<>();
        response.put("charts", charts);  // 차트 데이터 추가
        response.put("projects", projects);  // 프로젝트 데이터 추가

        return ResponseEntity.ok(response);  // JSON 형식으로 차트와 프로젝트 데이터 반환
    }

    // 차트 메인 페이지
    @GetMapping("/main")
    public String showMainPage(Model model) {
        return "chart/main";  // 차트 메인 페이지로 이동
    }

    // OKR 차트 페이지: 전체 목록 조회
    @GetMapping("/OKR")
    public String showOkrCharts(Model model) {
        List<ChartDTO> charts = chartService.getAllCharts();
        charts.sort(Comparator.comparing(ChartDTO::getChartUploadDate).reversed());  // 최신순으로 정렬

        model.addAttribute("charts", charts);  // 정렬된 차트를 모델에 추가
        return "chart/OKR";  // OKR 차트 페이지로 이동
    }

    // OKR 목표 상세 정보 조회
    @GetMapping("/okr/detail/{chartNo}")
    @ResponseBody
    public ResponseEntity<ChartDTO> getOkrDetail(@PathVariable Long chartNo) {
        ChartDTO chartDTO = chartService.getChartById(chartNo);  // 특정 차트 번호로 차트 조회
        if (chartDTO == null) {
            return ResponseEntity.notFound().build();  // 차트를 찾지 못한 경우 404 반환
        }
        return ResponseEntity.ok(chartDTO);  // 조회된 차트를 반환
    }

    // 차트 생성 등록
    @PostMapping("/create")
    public String createChart(ChartRequestDTO chartRequestDTO) {
        chartService.createChart(chartRequestDTO);  // 새로운 차트 생성
        log.info("Chart created with name: {}", chartRequestDTO.getChartName());
        return "redirect:/chart/OKR";  // 생성 후 OKR 차트 페이지로 리다이렉트
    }

    // OKR 목표 업데이트
    @PostMapping("/okr/update")
    public ResponseEntity<String> updateOkrChart(@RequestBody ChartRequestDTO chartDTO) {
        try {
            // 기존 데이터를 조회해서 author 값을 유지
            ChartDTO existingChart = chartService.getChartById(chartDTO.getChartNo());
            if (existingChart != null) {
                // 기존의 author를 유지하여 DTO에 설정
                chartDTO.setChartAuthor(existingChart.getChartAuthor());
            }

            // 나머지 업데이트 처리
            chartService.updateChart(chartDTO.getChartNo(), chartDTO);  // 차트 업데이트
            return ResponseEntity.ok("OKR chart updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating OKR chart");
        }
    }

    // 차트 수정 등록
    @PostMapping("/update")
    public String updateChart(ChartRequestDTO chartRequestDTO) {
        chartService.updateChart(chartRequestDTO.getChartNo(), chartRequestDTO);  // 차트 수정 처리
        return "redirect:/chart/OKR";  // 수정 후 OKR 차트 페이지로 리다이렉트
    }

    // 차트 삭제
    @PostMapping("/delete/{chartNo}")
    public String deleteChart(@PathVariable Long chartNo) {
        chartService.deleteChart(chartNo);  // 차트 삭제 처리
        return "redirect:/chart/OKR";  // 삭제 후 OKR 차트 페이지로 리다이렉트
    }

    // 프로젝트 차트 조회
    @GetMapping("/project")
    public String showProjectCharts(Model model) {
        // 모든 프로젝트를 가져와서 모델에 추가
        List<ProjectDTO> projectDTOList = projectService.getAllProjects();
        projectDTOList.sort(Comparator.comparing(ProjectDTO::getUploadDate).reversed());  // 최신순으로 정렬
        model.addAttribute("projects", projectDTOList);  // 정렬된 프로젝트를 모델에 추가
        return "chart/project";  // 프로젝트 차트 페이지로 이동
    }

    // 프로젝트 상세 정보 조회
    @GetMapping("/project/details/{projectNo}")
    public ResponseEntity<ProjectDTO> getProjectDetails(@PathVariable Long projectNo) {
        ProjectDTO project = projectService.getProjectById(projectNo);  // 특정 프로젝트 번호로 프로젝트 조회
        return ResponseEntity.ok(project);  // 조회된 프로젝트를 반환
    }

    // 차트 비교
    @PostMapping("/compare")
    @ResponseBody
    public ResponseEntity<List<ChartDTO>> compareCharts(@RequestBody List<Long> chartIds) {
        List<ChartDTO> chartData = chartService.getChartsByIds(chartIds);  // 전달된 차트 번호 목록으로 차트 데이터 조회
        return ResponseEntity.ok(chartData);  // 비교 결과 반환
    }

    // 페이지네이션 관련
    @GetMapping("/goals")
    public ResponseEntity<Page<ChartDTO>> getGoals(@RequestParam int page, @RequestParam int size) {
        Pageable pageable = PageRequest.of(page - 1, size);  // 페이지와 사이즈 설정
        Page<ChartDTO> goalsPage = chartService.getGoals(pageable);  // 목표 페이지 조회
        return ResponseEntity.ok(goalsPage);  // 조회된 목표 페이지 반환
    }

    // 목표 총 개수 조회
    @GetMapping("/goalCount")
    public ResponseEntity<Map<String, Integer>> getGoalCount() {
        try {
            int totalItems = chartService.getTotalGoalCount();  // 목표 총 개수 조회
            return ResponseEntity.ok(Collections.singletonMap("totalItems", totalItems));  // 총 개수 반환
        } catch (Exception e) {
            log.error("Error occurred while fetching goal count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
