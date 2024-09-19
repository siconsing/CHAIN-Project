package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.Data;

// 영민 추가
@Entity
@Data
@Table(name = "employee_leave")
public class EmployeeLeave {

    @Id
    @Column(name = "emp_no")
    private Long empNo;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "emp_no")
    private Employee employee;

    @Column(name = "total_leave_days")
    private int totalLeaveDays;

    @Column(name = "used_leave_days")
    private int usedLeaveDays;

    @Column(name = "unused_leave_days")
    private int unusedLeaveDays;

}

