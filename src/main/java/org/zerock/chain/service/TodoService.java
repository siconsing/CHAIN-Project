package org.zerock.chain.service;


import org.zerock.chain.dto.TodoDTO;
import org.zerock.chain.dto.TodoRequestDTO;

import java.util.List;

public interface TodoService {
    void saveOrUpdate(TodoRequestDTO todoRequestDTO);   // 등록
    List<TodoDTO> getAllTodos();    // 전체 목록
    List<TodoDTO> getFavoriteTodos()    ;   // 즐겨찾기
    List<TodoDTO> getCompletedTodos()   ;   // 완료
    List<TodoDTO> getIncompleteTodos(); // 미완료
    void updateTodoStatus(Long todoNo, boolean todoStatus); // 완료여부 상태 업데이트
    void deleteCompletedTodos(); // 완료항목 삭제
    void updateTodoFavoriteStatus(Long todoNo, boolean todoFavorite); // 즐겨찾기 상태 업데이트
    }


