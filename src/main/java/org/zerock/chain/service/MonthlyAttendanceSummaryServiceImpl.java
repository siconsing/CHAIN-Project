package org.zerock.chain.service;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.dto.AttendanceRecordDTO;
import org.zerock.chain.dto.MonthlyAttendanceSummaryDTO;
import org.zerock.chain.model.MonthlyAttendanceSummary;
import org.zerock.chain.repository.MonthlyAttendanceSummaryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class MonthlyAttendanceSummaryServiceImpl implements MonthlyAttendanceSummaryService {

    private final MonthlyAttendanceSummaryRepository summaryRepository;
    private final ModelMapper modelMapper;

    public MonthlyAttendanceSummaryServiceImpl(MonthlyAttendanceSummaryRepository summaryRepository,
                                               ModelMapper modelMapper) {
        this.summaryRepository = summaryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyAttendanceSummaryDTO> getAllSummariesByEmployee(Long empNo) {
        log.info("getAllSummariesByEmployee 호출됨 - 사원 번호: {}", empNo);

        List<MonthlyAttendanceSummary> summaries = summaryRepository.findByEmployeeEmpNoWithDetails(empNo);
        log.debug("DB 조회 결과: {}개의 요약 정보", summaries.size());

        // DTO로 변환하면서 attendances가 비어있지 않은 항목만 필터링
        return summaries.stream()
                .map(this::convertToDto)
                .filter(dto -> dto.getAttendances() != null && !dto.getAttendances().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyAttendanceSummaryDTO> getSummariesByYearAndMonth(int year, int month) {
        log.info("getSummariesByYearAndMonth 호출됨 - 연도: {}, 월: {}", year, month);

        List<MonthlyAttendanceSummary> summaries = summaryRepository.findByEmployeeEmpNoAndCurrentMonth(null, year, month);
        log.debug("DB 조회 결과: {}개의 요약 정보", summaries.size());

        // DTO로 변환하면서 출퇴근 기록(attendances)이 비어있지 않은 항목만 필터링
        return summaries.stream()
                .map(this::convertToDto)
                .filter(dto -> dto.getAttendances() != null && !dto.getAttendances().isEmpty())
                .collect(Collectors.toList());

    }


    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceSummaryDTO getSummaryById(Long id) {
        log.info("getSummaryById 호출됨 - ID: {}", id);
        MonthlyAttendanceSummary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID로 근태 요약을 찾을 수 없습니다: " + id));
        log.debug("DB 조회 성공: {}", summary);

        return convertToDto(summary);
    }

    private MonthlyAttendanceSummaryDTO convertToDto(MonthlyAttendanceSummary summary) {
        log.debug("convertToDto 호출됨 - 엔티티: {}", summary);
        MonthlyAttendanceSummaryDTO dto = modelMapper.map(summary, MonthlyAttendanceSummaryDTO.class);

        if (summary.getEmployee() != null) {
            dto.setEmpNo(summary.getEmployee().getEmpNo());
            dto.setFirstName(summary.getEmployee().getFirstName());
            dto.setLastName(summary.getEmployee().getLastName());

            if (summary.getEmployee().getDepartment() != null) {
                dto.setDepartmentName(summary.getEmployee().getDepartment().getDmpName());
            }
            if (summary.getEmployee().getRank() != null) {
                dto.setRankName(summary.getEmployee().getRank().getRankName());
            }

            dto.setAttendances(summary.getAttendanceRecords().stream()
                    .map(record -> modelMapper.map(record, AttendanceRecordDTO.class))
                    .collect(Collectors.toList()));
        }

        log.debug("DTO 생성 완료: {}", dto);
        return dto;
    }
}
