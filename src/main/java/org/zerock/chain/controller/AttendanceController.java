package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.AttendanceRecordDTO;
import org.zerock.chain.dto.MonthlyAttendanceSummaryDTO;
import org.zerock.chain.exception.AttendanceRecordNotFoundException;
import org.zerock.chain.service.AttendanceRecordService;
import org.zerock.chain.service.MonthlyAttendanceSummaryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequestMapping("/admin/attendance")
public class AttendanceController {

    private final MonthlyAttendanceSummaryService monthlyAttendanceSummaryService;
    private final AttendanceRecordService attendanceRecordService;

    public AttendanceController(MonthlyAttendanceSummaryService monthlyAttendanceSummaryService,
                                AttendanceRecordService attendanceRecordService) {
        this.monthlyAttendanceSummaryService = monthlyAttendanceSummaryService;
        this.attendanceRecordService = attendanceRecordService;
    }

    @GetMapping("/company")
    public String getCompanyAttendance(Model model) {
        log.info("getCompanyAttendance 호출됨");

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        List<MonthlyAttendanceSummaryDTO> summaries = monthlyAttendanceSummaryService.getSummariesByYearAndMonth(currentYear, currentMonth);
        Map<Integer, List<MonthlyAttendanceSummaryDTO>> summariesGroupedByMonth = summaries.stream()
                .collect(Collectors.groupingBy(MonthlyAttendanceSummaryDTO::getMonth));

        model.addAttribute("summariesGroupedByMonth", summariesGroupedByMonth);

        return "admin/attendance/company";
    }

    @GetMapping("/employee/{empNo}/monthly-summary")
    public String getEmployeeMonthlySummary(@PathVariable("empNo") Long empNo, Model model) {
        log.info("getEmployeeMonthlySummary 호출됨 - 사원 번호: {}", empNo);

        List<MonthlyAttendanceSummaryDTO> summaries = monthlyAttendanceSummaryService.getAllSummariesByEmployee(empNo);

        if (summaries == null || summaries.isEmpty()) {
            log.warn("summaries가 비어 있습니다. 사원 번호: {}", empNo);
        } else {
            log.info("summaries 데이터 크기: {}, 데이터: {}", summaries.size(), summaries);
        }

        model.addAttribute("summaries", summaries);

        return "admin/attendance/monthlySummary";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<AttendanceRecordDTO> getAttendanceRecordForEdit(@PathVariable("id") Long attendanceId) {
        log.info("getAttendanceRecordForEdit 호출됨 - 출근 기록 ID: {}", attendanceId);

        if (attendanceId == null || attendanceId <= 0) {
            log.warn("유효하지 않은 출근 기록 ID: {}", attendanceId);
            return ResponseEntity.badRequest().build();
        }

        AttendanceRecordDTO recordDTO = attendanceRecordService.getAttendanceById(attendanceId);
        if (recordDTO == null) {
            log.warn("출근 기록을 찾을 수 없습니다. ID: {}", attendanceId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(recordDTO);
    }
    @PostMapping("/edit")
    @ResponseBody
    public ResponseEntity<String> updateAttendanceRecord(@RequestParam("attendanceId") Long attendanceId,
                                                         @RequestParam("startTime") String startTime,
                                                         @RequestParam("endTime") String endTime,
                                                         @RequestParam("status") String status) {
        log.info("updateAttendanceRecord 호출됨 - 출근 기록 ID: {}, 시작 시간: {}, 종료 시간: {}, 상태: {}", attendanceId, startTime, endTime, status);

        attendanceRecordService.updateAttendanceRecord(attendanceId, startTime, endTime, status);

        return ResponseEntity.ok("출근 기록이 수정되었습니다.");
    }

    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<String> deleteAttendanceRecord(@RequestParam("attendanceId") Long attendanceId) {
        log.info("deleteAttendanceRecord 호출됨 - 출근 기록 ID: {}", attendanceId);

        attendanceRecordService.deleteAttendanceRecord(attendanceId);

        return ResponseEntity.ok("출근 기록이 삭제되었습니다.");
    }
    /**
     * 현재 사용자의 출근 상태를 반환하는 API
     */
    @GetMapping("/check-status")
    @ResponseBody
    public ResponseEntity<String> checkAttendanceStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String empNo = authentication.getName();

        if (empNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AttendanceRecordDTO record = attendanceRecordService.getAttendanceRecordByDateAndEmpNo(LocalDate.now(), Long.parseLong(empNo));

            if (record == null) {
                log.info("해당 날짜에 대한 출근 기록이 없습니다. 사원번호: {}", empNo);
                return ResponseEntity.ok("출근");
            }

            if (record.getEndTime() == null) {
                return ResponseEntity.ok("출근 중");
            }

            return ResponseEntity.ok("퇴근");

        } catch (AttendanceRecordNotFoundException e) {
            log.error("출근 상태 확인 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.ok("출근");  // 출근 기록이 없다고 예외가 발생하면 기본값으로 출근 상태 반환
        } catch (Exception e) {
            log.error("출근 상태 확인 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 출근 처리
     */
    @PostMapping("/check-in")
    public ResponseEntity<String> checkIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String empNo = authentication.getName();

        if (empNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사원번호를 찾을 수 없습니다.");
        }

        try {
            attendanceRecordService.recordCheckIn(Long.parseLong(empNo));
            return ResponseEntity.ok("출근입니다!.");
        } catch (IllegalStateException e) {
            log.error("사원번호 {} 출근 처리 중 오류 발생: {}", empNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("사원번호 {} 출근 처리 중 오류 발생: {}", empNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("출근 기록 생성에 실패했습니다.");
        }
    }

    /**
     * 퇴근 처리
     */
    @PostMapping("/check-out")
    public ResponseEntity<String> checkOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String empNo = authentication.getName();

        if (empNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사원번호를 찾을 수 없습니다.");
        }

        try {
            attendanceRecordService.recordCheckOut(Long.parseLong(empNo));
            return ResponseEntity.ok("퇴근입니다!");
        } catch (IllegalStateException e) {
            log.error("사원번호 {} 퇴근 처리 중 오류 발생: {}", empNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("사원번호 {} 퇴근 처리 중 오류 발생: {}", empNo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("퇴근 기록 생성에 실패했습니다.");
        }
    }
}
