package org.zerock.chain.dto;


import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long empNo;
    private String firstName;
    private String lastName;
    private String phoneNum;
    private LocalDate birthDate;
    private String addr;
    private String email;
    private LocalDate hireDate;
    private LocalDate lastDate;
    private Long departmentId;
    private Long rankId;
    private String departmentName;
    private String rankName;
    private List<Long> permissionIds;


}