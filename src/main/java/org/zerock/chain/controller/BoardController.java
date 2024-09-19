package org.zerock.chain.controller;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.BoardDTO;
import org.zerock.chain.dto.BoardRequestDTO;
import org.zerock.chain.service.BoardService;

@Controller
@RequestMapping("/board")
@Log4j2
public class BoardController {

    @Autowired
    private final BoardService boardService;  // 게시글 서비스 계층 의존성 주입

    public BoardController(BoardService boardService) {
        this.boardService = boardService;  // 게시글 서비스 초기화
    }

    // 구내식당 페이지로 이동
    @GetMapping("/cafeteria")
    public String boardCafeteria() {
        return "board/cafeteria";
    }

    // 경조사 게시판 전체 목록 조회
    @GetMapping("/list")
    public String boardList(Model model) {
        model.addAttribute("boards", boardService.getAllBoards());  // 전체 게시글 목록을 모델에 추가
        return "board/list";  // 목록 페이지로 이동
    }

    // 경조사 문서 개별 확인 페이지
    @GetMapping("/detail/{boardNo}")
    public String boardDetail(@PathVariable("boardNo") Long boardNo, Model model) {
        BoardDTO boardDTO = boardService.getBoardById(boardNo);  // 개별 게시글 조회
        System.out.println("Board Location: " + boardDTO.getBoardLocation());  // 게시글 위치 정보 출력
        model.addAttribute("board", boardDTO);  // 조회된 게시글을 모델에 추가
        return "board/detail";  // 상세 페이지로 이동
    }

    // 경조사 문서 생성 페이지
    @GetMapping("/register")
    public String boardRegister(Model model) {
        model.addAttribute("boardRequestDTO", new BoardRequestDTO());  // 빈 DTO를 모델에 추가하여 등록 폼에 바인딩
        return "board/register";  // 등록 페이지로 이동
    }

    // 경조사 게시글 생성
    @PostMapping("/register")
    public String createBoard(@Valid @ModelAttribute BoardRequestDTO boardRequestDTO, BindingResult result, Model model) {
        BoardDTO createdBoard = boardService.createBoard(boardRequestDTO);  // 새로운 게시글 생성
        return "redirect:/board/detail/" + createdBoard.getBoardNo();  // 생성된 게시글의 상세 페이지로 리다이렉트
    }

    // 경조사 문서 수정 페이지
    @GetMapping("/modify/{boardNo}")
    public String boardModify(@PathVariable("boardNo") Long boardNo, Model model) {
        model.addAttribute("board", boardService.getBoardById(boardNo));  // 수정할 게시글을 모델에 추가
        return "board/modify";  // 수정 페이지로 이동
    }

    // 경조사 게시글 수정
    @PostMapping("/modify/{boardNo}")
    public String updateBoard(@PathVariable("boardNo") Long boardNo, @ModelAttribute BoardRequestDTO boardRequestDTO) {
        BoardDTO existingBoard = boardService.getBoardById(boardNo);  // 기존 게시글을 조회

        boardRequestDTO.setBoardCategory(existingBoard.getBoardCategory());  // 기존 카테고리를 그대로 유지

        boardService.updateBoard(boardNo, boardRequestDTO);  // 게시글 수정 처리
        return "redirect:/board/detail/" + boardNo;  // 수정된 게시글의 상세 페이지로 리다이렉트
    }

    // 경조사 게시글 삭제
    @PostMapping("/delete/{boardNo}")
    public String deleteNotice(@PathVariable("boardNo") Long boardNo) {
        boardService.deleteBoard(boardNo);  // 게시글 삭제 처리
        return "redirect:/board/list";  // 목록 페이지로 리다이렉트
    }
}
