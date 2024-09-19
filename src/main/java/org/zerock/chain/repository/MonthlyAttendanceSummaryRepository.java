    package org.zerock.chain.repository;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.zerock.chain.model.MonthlyAttendanceSummary;

    import java.util.List;

    public interface MonthlyAttendanceSummaryRepository extends JpaRepository<MonthlyAttendanceSummary, Long> {

        @Query("SELECT DISTINCT mas FROM MonthlyAttendanceSummary mas " +
                "JOIN FETCH mas.employee e " +
                "LEFT JOIN FETCH mas.attendanceRecords ar " +
                "WHERE e.empNo = :empNo")
        List<MonthlyAttendanceSummary> findByEmployeeEmpNoWithDetails(@Param("empNo") Long empNo);

        @Query("SELECT DISTINCT mas FROM MonthlyAttendanceSummary mas " +
                "JOIN FETCH mas.employee e " +
                "LEFT JOIN FETCH mas.attendanceRecords ar " +
                "WHERE (:empNo IS NULL OR e.empNo = :empNo) AND mas.year = :year AND mas.month = :month")
        List<MonthlyAttendanceSummary> findByEmployeeEmpNoAndCurrentMonth(@Param("empNo") Long empNo,
                                                                          @Param("year") int year,
                                                                          @Param("month") int month);

        // 기본 메서드 사용
        List<MonthlyAttendanceSummary> findByEmployeeEmpNo(Long empNo);
    }
