package org.zerock.chain.service;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.dto.AttendanceRecordDTO;
import org.zerock.chain.exception.AttendanceRecordNotFoundException;
import org.zerock.chain.model.AttendanceRecord;
import org.zerock.chain.model.AttendanceStatus;
import org.zerock.chain.model.Employee;
import org.zerock.chain.repository.AttendanceRecordRepository;
import org.zerock.chain.repository.EmployeeRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public AttendanceRecordServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                       EmployeeRepository employeeRepository,
                                       ModelMapper modelMapper) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public void recordCheckIn(Long empNo) {
        log.info("사원번호 {} 출근 기록 생성 시도", empNo);

        // 오늘 이미 출근 기록이 있는지 확인
        if (attendanceRecordRepository.findFirstByEmployee_EmpNoAndAttendanceDateOrderByStartTimeDesc(empNo, LocalDate.now()).isPresent()) {
            throw new IllegalStateException("이미 출근 기록이 존재합니다.");
        }

        Employee employee = employeeRepository.findById(empNo)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사원 ID입니다."));

        AttendanceRecord attendance = new AttendanceRecord();
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(LocalDate.now());
        attendance.setStartTime(LocalTime.now());

        // 출근 상태 설정
        attendance.setStatus(determineAttendanceStatus(attendance.getStartTime()));

        attendanceRecordRepository.save(attendance);

        log.info("사원번호 {} 출근 기록 생성 성공", empNo);
    }
    @Transactional
    @Override
    public void recordCheckOut(Long empNo) {
        log.info("사원번호 {} 퇴근 기록 생성 시도", empNo);

        // 오늘의 출근 기록을 조회하여 가져오기
        AttendanceRecord attendance = attendanceRecordRepository.findFirstByEmployee_EmpNoAndAttendanceDateOrderByStartTimeDesc(empNo, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("오늘 출근 기록이 없습니다."));

        // 이미 퇴근 기록이 있는지 확인
        if (attendance.getEndTime() != null) {
            throw new IllegalStateException("이미 퇴근 기록이 존재합니다.");
        }

        // 퇴근 시간과 상태 설정
        attendance.setEndTime(LocalTime.now());
        attendance.setStatus(AttendanceStatus.퇴근);

        // 총 근무 시간 계산
        int totalWorkingHours = (int) Duration.between(attendance.getStartTime(), attendance.getEndTime()).toHours();
        attendance.setTotalWorkingHours(totalWorkingHours);

        // 연장 근무 시간 계산 (예: 8시간을 초과하는 근무 시간은 연장 근무로 간주)
        int overtimeHours = Math.max(0, totalWorkingHours - 8); // 8시간 이상 근무한 시간이 연장 근무 시간
        attendance.setOvertimeHours(overtimeHours);

        attendanceRecordRepository.save(attendance);

        log.info("사원번호 {} 퇴근 기록 생성 성공 - 총 근무 시간: {}시간, 연장 근무 시간: {}시간", empNo, totalWorkingHours, overtimeHours);
    }

    @Override
    @Transactional
    public void updateAttendanceRecord(Long attendanceId, String startTime, String endTime, String status) {
        // attendanceId로 해당 출근 기록 조회
        AttendanceRecord record = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("출근 기록을 찾을 수 없습니다. ID: " + attendanceId));

        // 출근 시간과 퇴근 시간 설정 (초단위 제거)
        record.setStartTime(LocalTime.parse(startTime).withSecond(0).withNano(0));
        record.setEndTime(LocalTime.parse(endTime).withSecond(0).withNano(0));

        // 근태 상태 설정
        record.setStatus(AttendanceStatus.valueOf(status));

        // 총 근무 시간 계산
        int totalWorkingHours = (int) Duration.between(record.getStartTime(), record.getEndTime()).toHours();
        record.setTotalWorkingHours(totalWorkingHours);

        // 연장 근무 시간 계산 (예: 8시간을 초과하는 근무 시간은 연장 근무로 간주)
        int overtimeHours = Math.max(0, totalWorkingHours - 8);
        record.setOvertimeHours(overtimeHours);

        // 변경된 출퇴근 기록 저장
        attendanceRecordRepository.save(record);

        log.info("출근 기록이 업데이트되었습니다. ID: {}, 총 근무 시간: {}, 연장 근무 시간: {}", attendanceId, totalWorkingHours, overtimeHours);
    }



    @Override
    @Transactional
    public void deleteAttendanceRecord(Long attendanceId) {
        AttendanceRecord record = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("유효하지 않은 출근 기록 ID입니다."));

        attendanceRecordRepository.delete(record);
    }

    @Override
    public AttendanceRecordDTO getAttendanceById(Long attendanceId) {
        if (attendanceId == null || attendanceId <= 0) {
            log.warn("유효하지 않은 출근 기록 ID: {}", attendanceId);
            throw new AttendanceRecordNotFoundException("출근 기록을 찾을 수 없습니다. ID: " + attendanceId);
        }

        AttendanceRecord record = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("출근 기록을 찾을 수 없습니다. ID: " + attendanceId));

        return convertToDto(record);
    }


    @Override
    public AttendanceRecordDTO getAttendanceRecordByDateAndEmpNo(LocalDate date, Long empNo) {
        AttendanceRecord record = attendanceRecordRepository
                .findFirstByEmployee_EmpNoAndAttendanceDateOrderByStartTimeDesc(empNo, date)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("해당 날짜에 대한 출근 기록이 없습니다."));

        return convertToDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getMonthlyAttendanceRecords(Long empNo, int year, int month) {
        List<AttendanceRecord> records = attendanceRecordRepository
                .findAllByEmployee_EmpNoAndAttendanceDateBetween(empNo,
                        LocalDate.of(year, month, 1),
                        LocalDate.of(year, month, LocalDate.of(year, month, 1).lengthOfMonth())
                );

        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AttendanceRecordDTO convertToDto(AttendanceRecord record) {
        return modelMapper.map(record, AttendanceRecordDTO.class);
    }

    private AttendanceStatus determineAttendanceStatus(LocalTime checkInTime) {
        LocalTime standardCheckInTime = LocalTime.of(9, 0);
        return checkInTime.isAfter(standardCheckInTime) ? AttendanceStatus.지각 : AttendanceStatus.출근;
    }
}
