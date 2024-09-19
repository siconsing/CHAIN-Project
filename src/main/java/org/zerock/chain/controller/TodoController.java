// TodoController.java
package org.zerock.chain.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.TodoDTO;
import org.zerock.chain.dto.TodoRequestDTO;
import org.zerock.chain.service.TodoService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequestMapping("/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;  // Todo 서비스 계층 의존성 주입

    // 리스트 등록
    @PostMapping("/add")
    public String addTodo(TodoRequestDTO todoRequestDTO) {
        todoService.saveOrUpdate(todoRequestDTO);  // 새로운 Todo 저장 또는 업데이트
        return "redirect:/todo/list";  // 리스트 페이지로 리다이렉트
    }

    // 리스트 조회
    @GetMapping("/list")
    public String list(Model model) {
        // 전체 목록을 최신순으로 정렬
        List<TodoDTO> todos = todoService.getAllTodos().stream()
                .sorted(Comparator.comparing(TodoDTO::getTodoCreatedDate).reversed())
                .collect(Collectors.toList());

        // 오늘 날짜만 필터링
        LocalDate today = LocalDate.now();
        List<TodoDTO> todayTodos = todos.stream()
                .filter(todo -> todo.getTodoCreatedDate().equals(today))
                .collect(Collectors.toList());

        // 즐겨찾기 목록 필터링
        List<TodoDTO> favoriteTodos = todoService.getFavoriteTodos();

        // 완료된 항목 필터링
        List<TodoDTO> completedTodos = todoService.getCompletedTodos();

        // 진행 중인 항목 필터링
        List<TodoDTO> incompleteTodos = todoService.getIncompleteTodos();

        model.addAttribute("todos", todos);  // 전체 목록을 모델에 추가
        model.addAttribute("todayTodos", todayTodos);  // 오늘 날짜 항목을 모델에 추가
        model.addAttribute("favoriteTodos", favoriteTodos);  // 즐겨찾기 목록을 모델에 추가
        model.addAttribute("completedTodos", completedTodos);  // 완료된 항목을 모델에 추가
        model.addAttribute("incompleteTodos", incompleteTodos);  // 진행 중인 항목을 모델에 추가

        return "todo/list";  // Todo 리스트 페이지로 이동
    }

    // 완료 리스트 삭제
    @GetMapping("/deleteCompleted")
    public String deleteCompletedTodos() {
        todoService.deleteCompletedTodos();  // 완료된 항목 삭제 처리
        return "redirect:/todo/list";  // 삭제 후 리스트 페이지로 리다이렉트
    }

    // 상태 업데이트 (완료 여부)
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam("todoNo") Long todoNo, @RequestParam("todoStatus") boolean todoStatus) {
        todoService.updateTodoStatus(todoNo, todoStatus);  // 완료 상태 업데이트
        return "redirect:/todo/list";  // 업데이트 후 리스트 페이지로 리다이렉트
    }

    // 즐겨찾기 상태 업데이트
    @PostMapping("/updateFavoriteStatus")
    public String updateFavoriteStatus(@RequestParam("todoNo") Long todoNo, @RequestParam("todoFavorite") boolean todoFavorite) {
        todoService.updateTodoFavoriteStatus(todoNo, todoFavorite);  // 즐겨찾기 상태 업데이트
        return "redirect:/todo/list";  // 업데이트 후 리스트 페이지로 리다이렉트
    }
}
