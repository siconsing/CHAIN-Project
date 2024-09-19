package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Todo;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 특정 사원 번호(empNo)에 해당하는 모든 Todo 항목을 찾는 메서드
    List<Todo> findByEmpNo(Long empNo);
    // 특정 사원 번호(empNo)와 즐겨찾기 상태(todoFavorite)에 해당하는 Todo 항목을 찾는 메서드
    List<Todo> findByEmpNoAndTodoFavorite(Long empNo, boolean todoFavorite);
    // 특정 사원 번호(empNo)와 완료 상태(todoStatus)에 해당하는 Todo 항목을 찾는 메서드
    List<Todo> findByEmpNoAndTodoStatus(Long empNo, boolean todoStatus);
}
