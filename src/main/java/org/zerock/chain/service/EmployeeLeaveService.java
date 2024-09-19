package org.zerock.chain.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.model.EmployeeLeave;
import org.zerock.chain.repository.EmployeeLeaveRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 영민 추가
public class EmployeeLeaveService {

    private final EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    public EmployeeLeaveService(EmployeeLeaveRepository employeeLeaveRepository) {
        this.employeeLeaveRepository = employeeLeaveRepository;
    }

    // 사원별 연차 정보 가져오는 메서드
    public EmployeeLeave getEmployeeLeaveByEmpNo(Long empNo) {
        return employeeLeaveRepository.findByEmpNo(empNo);
    }

    // 관리자가 업데이트한 연차 계산해서 업데이트 후 DB에 저장하는 메서드
    public Map<String, Integer> updateUsedLeaveDays(Long empNo, int usedLeaveDays) {
        EmployeeLeave employeeLeave = employeeLeaveRepository.findByEmpNo(empNo);
        if (employeeLeave != null) {
            employeeLeave.setUsedLeaveDays(usedLeaveDays);

            // 미사용 연차 계산
            int unusedLeaveDays = employeeLeave.getTotalLeaveDays() - usedLeaveDays;
            employeeLeave.setUnusedLeaveDays(unusedLeaveDays);

            employeeLeaveRepository.save(employeeLeave);

            // 반환할 데이터
            Map<String, Integer> updatedLeaveData = new HashMap<>();
            updatedLeaveData.put("totalLeaveDays", employeeLeave.getTotalLeaveDays());
            updatedLeaveData.put("usedLeaveDays", usedLeaveDays);
            updatedLeaveData.put("unusedLeaveDays", unusedLeaveDays);

            return updatedLeaveData;
        }
        return null;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")  // << 매월 1일 자정에 실행 | 발표/설명할때 >> 1분마다 실행 (cron = "0 * * * * *")
    public void addMonthlyLeaveDays() {
        List<EmployeeLeave> allEmployeeLeaves = employeeLeaveRepository.findAll();
        for (EmployeeLeave employeeLeave : allEmployeeLeaves) {
            int newTotalLeaveDays = employeeLeave.getTotalLeaveDays() + 1;
            employeeLeave.setTotalLeaveDays(newTotalLeaveDays);

            // 미사용 연차 증가 (새로 추가된 1일을 미사용 연차에 반영)
            employeeLeave.setUnusedLeaveDays(employeeLeave.getUnusedLeaveDays() + 1);

            // 저장
            employeeLeaveRepository.save(employeeLeave);
        }
    }

}

