package org.zerock.chain.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.service.ChatUserService;

import java.util.List;
import java.util.Optional;

@Log4j2
@Controller
@RequiredArgsConstructor
public class ChatUserController {
    private final ChatUserService chatUserService;

    @MessageMapping("/user.addUser")
    @SendTo("/topic/public")
    public EmployeeDTO addUser(@Payload Long empNo) {
        // 사원 번호로 사원 정보 조회
        Optional<EmployeeDTO> employeeDTO = chatUserService.findEmployeeByEmpNo(empNo);

        if (employeeDTO.isPresent()) {
            return employeeDTO.orElse(null);
        } else {
            return null; // 혹은 적절한 처리
        }
    }

    @MessageMapping("/user.disconnectUser") // 연결 끊김
    @SendTo("/topic/public")
    public Long disconnectUser(@Payload Long empNo) {
        return empNo;
    }

    // 대화 중인 사용자만 반환(채팅방 호출)
    @GetMapping("/chat/activeUsers")
    public ResponseEntity<List<EmployeeDTO>> findActiveChatUsers(@RequestParam("empNo") Long empNo) {
        List<EmployeeDTO> activeUsers = chatUserService.findActiveChatUsers(empNo);
        return ResponseEntity.ok(activeUsers);
    }

    // /employees/{empNo} 경로로 사원 정보를 가져오는 코드가 필요
    @GetMapping("/employees/{empNo}")
    public ResponseEntity<EmployeeDTO> getEmployeeByEmpNo(@PathVariable("empNo") Long empNo) {
        Optional<EmployeeDTO> employeeDTO = chatUserService.findEmployeeByEmpNo(empNo);
        return employeeDTO.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
