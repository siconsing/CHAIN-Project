package org.zerock.chain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.repository.ChatRoomRepository;
import org.zerock.chain.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatUserService {
    private final ChatRoomRepository chatRoomRepository;
    private final EmployeeRepository employeeRepository;

    public void saveEmployee(Long empNo) { // 사용자 연결시
    }

    public void disconnect(Long empNo) { // 사용자 연결 끊을 시
    }

    // 사원 번호로 사원 정보를 조회하는 메서드
    public Optional<EmployeeDTO> findEmployeeByEmpNo(Long empNo) {
        return employeeRepository.findById(empNo)
                .map(employee -> EmployeeDTO.builder()
                        .empNo(employee.getEmpNo())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .rankName(employee.getRank().getRankName()) // 직급 이름 가져오기
                        .build());
    }

    // 대화 중인 사용자 목록 반환
    public List<EmployeeDTO> findActiveChatUsers(Long empNo) {
        // Employee 리스트에서 empNo만 추출하여 리스트로 반환
        return chatRoomRepository.findActiveChatUsersByEmpNo(empNo)
                .stream()
                .map(employee -> EmployeeDTO.builder()
                        .empNo(employee.getEmpNo())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .rankName(employee.getRank().getRankName()) // Department의 이름 가져오기
                        .build())
                .collect(Collectors.toList());
    }
}

