package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.chain.model.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    Optional<AttendanceRecord> findFirstByEmployee_EmpNoAndAttendanceDateOrderByStartTimeDesc(Long empNo, LocalDate attendanceDate);
    List<AttendanceRecord> findAllByEmployee_EmpNoAndAttendanceDateBetween(Long empNo, LocalDate startDate, LocalDate endDate);
}
