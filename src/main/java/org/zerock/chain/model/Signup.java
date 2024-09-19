package org.zerock.chain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "employees")
public class Signup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emp_no")
    private Long empNo;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "password")
    private String password;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "phone_num")
    private String phoneNum;

    @Column(name = "email")
    private String email;

    @Column(name = "addr")
    private String addr;

    @Column(name = "hire_date")
    private LocalDate hireDate;

/*    // 박성은 추가 코드
    @Column(name = "rank_no")
    private Long rankNo;*/
    @Column(name = "dmp_no")
    private Long dmpNo = 0L; // 기본값 0 설정

    @Column(name = "rank_no")
    private Long rankNo = 1L; // 기본값 1 설정

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EmployeePermission> employeePermissions = new ArrayList<>();

    // 기타 필요한 필드들...
}
