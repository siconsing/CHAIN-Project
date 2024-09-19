package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zerock.chain.dto.EmployeeDTO;
import org.zerock.chain.model.Department;
import org.zerock.chain.repository.DepartmentRepository;
import org.zerock.chain.service.EmployeeService;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Controller
public class ChatPageController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentRepository departmentRepository;

    // 조직도
    @GetMapping("/chatting")
    public String chat(@RequestParam("empNo") String empNo, Model model) {
        // index.html에서 empNo의 세션값 받아서 empNo를 모델에 추가하여 템플릿에 전달
        model.addAttribute("empNo", empNo);

        // 모든 부서를 가져옴 (DepartmentId로 정렬)
        List<Department> departments = departmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Department::getDmpNo))  // DepartmentId로 부서 정렬
                .collect(Collectors.toList());

        // 부서별로 사원들을 그룹화하여 저장할 맵
        Map<String, List<EmployeeDTO>> departmentMap = new LinkedHashMap<>();  // LinkedHashMap 사용

        // 각 부서에 속한 사원들을 가져와서 맵에 추가
        for (Department department : departments) {
            List<EmployeeDTO> employees = employeeService.getEmployeesByDepartmentId(department.getDmpNo());

            // 사원들을 RankId로 정렬
            List<EmployeeDTO> sortedEmployees = employees.stream()
                    .sorted(Comparator.comparing(EmployeeDTO::getRankId))
                    .collect(Collectors.toList());

            departmentMap.put(department.getDmpName(), sortedEmployees);
        }

        // 모델에 부서명과 사원 데이터를 추가
        model.addAttribute("departmentMap", departmentMap);

        return "chatting";
    }

}
