package org.zerock.chain.service;

import org.zerock.chain.dto.AttendanceRecordDTO;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordService {
    void recordCheckIn(Long empNo);
    void recordCheckOut(Long empNo);
    void updateAttendanceRecord(Long attendanceId, String startTime, String endTime, String status);
    void deleteAttendanceRecord(Long attendanceId);
    AttendanceRecordDTO getAttendanceById(Long attendanceId);
    AttendanceRecordDTO getAttendanceRecordByDateAndEmpNo(LocalDate date, Long empNo);
    List<AttendanceRecordDTO> getMonthlyAttendanceRecords(Long empNo, int year, int month);
}
