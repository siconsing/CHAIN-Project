package org.zerock.chain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Data
public class AttendanceRecordDTO {
    private Long id;
    // empNo 필드를 제거하였습니다.
    private LocalDate attendanceDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int totalWorkingHours;
    private int overtimeHours;
    private String status;
    // 휴게 시간을 관리하기 위해 breakTime 필드를 추가할 수 있습니다.
    private int breakTime;
    public String getFormattedStartTime() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }

    public String getFormattedEndTime() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("HH:mm")) : "";
    }
}
