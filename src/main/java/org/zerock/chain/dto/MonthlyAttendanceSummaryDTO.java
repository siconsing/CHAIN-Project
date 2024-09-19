package org.zerock.chain.dto;

import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAttendanceSummaryDTO {

    private Long id;  // 월간 근태 요약 ID
    private Long empNo;  // 사원 번호
    private String firstName;  // 직원 성
    private String lastName;  // 직원 이름
    private int year;  // 연도
    private int month;  // 월
    private int totalWorkingDays;  // 총 근무 일수
    private int lateDays;  // 지각 일수
    private int absentDays;  // 결근 일수
    private int overtimeHours;  // 연장 근무 시간
    private int leaveDays = 0; // 연차일수 기본값 0
    private int remainingLeaveDays = 0; // 남은 연차일수 기본값 0
    private String departmentName;  // 부서명
    private String rankName;  // 직급명
    private List<AttendanceRecordDTO> attendances;  // 해당 월의 출퇴근 기록 리스트

}
