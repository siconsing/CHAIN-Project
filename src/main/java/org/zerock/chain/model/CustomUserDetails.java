package org.zerock.chain.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long empNo;
    private final String firstName;
    private final String lastName;
    private final String rankName;
    private final String departmentName; // 부서 이름 필드 추가

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, Long empNo, String firstName, String lastName, String rankName, String departmentName) {
        super(username, password, authorities);
        this.empNo = empNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.rankName = rankName;
        this.departmentName = departmentName; // 부서 이름 설정
    }

    public Long getEmpNo() {
        return empNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRankName() {
        return rankName;
    }

    public String getDepartmentName() {
        return departmentName; // 부서 이름 getter 메서드 추가
    }
}
