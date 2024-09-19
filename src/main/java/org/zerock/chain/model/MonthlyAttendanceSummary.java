package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_attendance_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyAttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_no", nullable = false)
    private Employee employee;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "total_working_days")
    private int totalWorkingDays;

    @Column(name = "late_days")
    private int lateDays;

    @Column(name = "absent_days")
    private int absentDays;

    @Column(name = "overtime_hours")
    private int overtimeHours;

    @OneToMany(mappedBy = "monthlySummary", cascade = CascadeType.ALL)
    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

}
