package org.zerock.chain.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zerock.chain.dto.TodoDTO;
import org.zerock.chain.dto.TodoRequestDTO;
import org.zerock.chain.model.Todo;
import org.zerock.chain.repository.TodoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl extends BaseService<Todo> implements TodoService {

    private final TodoRepository todoRepository;
    private final ModelMapper modelMapper;

    // 사원 번호 기반의 모든 ToDo 항목 조회
    @Override
    protected List<Todo> getAllItemsByEmpNo(Long empNo) {
        return todoRepository.findByEmpNo(empNo);
    }

    // 리스트 등록 또는 업데이트
    @Override
    public void saveOrUpdate(TodoRequestDTO todoRequestDTO) {
        Long empNo = getEmpNoFromSession();
        Todo todo = modelMapper.map(todoRequestDTO, Todo.class);
        todo.setEmpNo(empNo);
        todoRepository.save(todo);
    }

    // 전체 리스트 조회
    @Override
    public List<TodoDTO> getAllTodos() {
        Long empNo = getEmpNoFromSession();
        return getItemsByEmpNo(empNo, todoRepository::findByEmpNo).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 즐겨찾기 리스트 조회
    @Override
    public List<TodoDTO> getFavoriteTodos() {
        Long empNo = getEmpNoFromSession();
        return todoRepository.findByEmpNoAndTodoFavorite(empNo, true).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 완료 상태 리스트 조회
    @Override
    public List<TodoDTO> getCompletedTodos() {
        Long empNo = getEmpNoFromSession();
        return todoRepository.findByEmpNoAndTodoStatus(empNo, true).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 미완료 상태 리스트 조회
    @Override
    public List<TodoDTO> getIncompleteTodos() {
        Long empNo = getEmpNoFromSession();
        return todoRepository.findByEmpNoAndTodoStatus(empNo, false).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 완료된 ToDo 삭제
    @Override
    public void deleteCompletedTodos() {
        Long empNo = getEmpNoFromSession();
        List<Todo> completedTodos = todoRepository.findByEmpNoAndTodoStatus(empNo, true);
        todoRepository.deleteAll(completedTodos);
    }

    // ToDo 완료 상태 업데이트
    @Override
    public void updateTodoStatus(Long todoNo, boolean todoStatus) {
        Todo todo = todoRepository.findById(todoNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid todo ID: " + todoNo));
        todo.setTodoStatus(todoStatus);
        todoRepository.save(todo);
    }

    // 즐겨찾기 상태 업데이트
    @Override
    public void updateTodoFavoriteStatus(Long todoNo, boolean todoFavorite) {
        Todo todo = todoRepository.findById(todoNo)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.setTodoFavoriteStatus(todoFavorite);
        todoRepository.save(todo);
    }

    // DTO 변환
    private TodoDTO convertToDto(Todo todo) {
        return modelMapper.map(todo, TodoDTO.class);
    }

    // 세션에서 사원 번호를 가져오는 메서드
    private Long getEmpNoFromSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        return (Long) session.getAttribute("empNo");  // 세션에 저장된 사원 번호 가져오기
    }
}
