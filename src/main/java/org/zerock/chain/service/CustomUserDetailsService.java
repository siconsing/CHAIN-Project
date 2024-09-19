package org.zerock.chain.service;

import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.model.*;
import org.zerock.chain.repository.SignupRepository;
import org.zerock.chain.repository.RankRepository;
import org.zerock.chain.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SignupRepository signupRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private DepartmentRepository departmentRepository;  // 부서 리포지토리 추가

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long empNo = Long.valueOf(username);
            Signup user = signupRepository.findByEmpNo(empNo)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with empNo: " + empNo));

            // rankNo를 통해 rankName 조회
            Rank rank = rankRepository.findById(user.getRankNo())
                    .orElseThrow(() -> new UsernameNotFoundException("Rank not found with rankNo: " + user.getRankNo()));
            String rankName = rank.getRankName();

            // dmp_no를 통해 부서 조회
            Department department = departmentRepository.findById(user.getDmpNo())
                    .orElseThrow(() -> new UsernameNotFoundException("Department not found with dmpNo: " + user.getDmpNo()));
            String departmentName = department.getDmpName();

            // 권한 목록을 가져옴
            List<GrantedAuthority> authorities = user.getEmployeePermissions().stream()
                    .map(EmployeePermission::getPermission)
                    .map(Permission::getPerName)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 기본 권한 ROLE_USER 추가
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            // 사원이름, 직급, 부서 이름을 CustomUserDetails에 전달
            return new CustomUserDetails(
                    user.getEmpNo().toString(),
                    user.getPassword(),
                    authorities,
                    user.getEmpNo(),
                    user.getFirstName(),
                    user.getLastName(),
                    rankName,
                    departmentName  // 부서 이름 추가
            );
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Username must be a numeric value representing empNo.");
        }
    }
}
