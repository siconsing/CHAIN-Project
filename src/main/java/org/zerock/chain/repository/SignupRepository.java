package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zerock.chain.model.Signup;

import java.util.Optional;

public interface SignupRepository extends JpaRepository<Signup, Long> {
    Optional<Signup> findByEmpNo(Long empNo);
    Optional<Signup> findByEmailIgnoreCase(String email);  // 이메일 중복 체크
    Optional<Signup> findByPhoneNum(String phoneNum);  // 전화번호 중복 체크
    // 가장 큰 emp_no를 가져오는 쿼리
    @Query("SELECT MAX(e.empNo) FROM Employee e")
    Long findMaxEmpNo();
}
