package org.zerock.chain.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zerock.chain.dto.CommentDTO;
import org.zerock.chain.dto.CommentRequestDTO;
import org.zerock.chain.service.CommentService;
import org.zerock.chain.service.QnaService;

import java.util.List;

@Controller
@RequestMapping("/comment")
@Log4j2
public class CommentController {

    private final CommentService commentService;  // 댓글 서비스 계층 의존성 주입
    private final QnaService qnaService;  // Q&A 서비스 계층 의존성 주입

    // 생성자 주입 방식으로 변경
    public CommentController(CommentService commentService, QnaService qnaService) {
        this.commentService = commentService;  // CommentService 초기화
        this.qnaService = qnaService;  // QnaService 초기화
    }

    // 특정 문의글 번호에 해당하는 모든 댓글을 가져오는 메서드
    @GetMapping("/qna/{qnaNo}")
    public String getComments(@PathVariable("qnaNo") Long qnaNo, Model model) {
        List<CommentDTO> comments = commentService.getCommentsByQnaNo(qnaNo);  // 특정 QnA 번호에 대한 모든 댓글 조회
        model.addAttribute("comments", comments);  // 댓글 리스트를 모델에 추가
        model.addAttribute("qnaNo", qnaNo);  // QnA 번호를 모델에 추가
        return "user/qaDetail";  // Q&A 상세 페이지로 이동하며 댓글 리스트를 전달
    }

    // 특정 문의글에 새로운 댓글을 추가하는 메서드
    @PostMapping("/qna/{qnaNo}/add")
    public String addComment(@PathVariable("qnaNo") Long qnaNo, @ModelAttribute CommentRequestDTO commentRequestDTO) {
        commentRequestDTO.setQnaNo(qnaNo);  // QnA 번호를 댓글 요청 DTO에 설정
        try {
            commentService.addComment(commentRequestDTO);  // 새로운 댓글 추가
            log.info("New comment added successfully for QnA ID: {}", qnaNo);

            // 댓글 추가 후 QnA 상태를 true로 업데이트
            qnaService.updateQnaStatus(qnaNo, true);
        } catch (Exception e) {
            log.error("Error adding comment for QnA ID: {}", qnaNo, e);
            return "redirect:/error";  // 에러 페이지로 리다이렉트
        }

        return "redirect:/user/qna/detail/" + qnaNo;  // 성공적으로 추가된 경우 QnA 상세 페이지로 리다이렉트
    }

    // 댓글 수정 메서드
    @PostMapping("/edit/{commentNo}")
    public String editComment(@PathVariable("commentNo") Long commentNo, @ModelAttribute CommentRequestDTO commentRequestDTO, @RequestParam("qnaNo") Long qnaNo) {
        try {
            commentService.updateComment(commentNo, commentRequestDTO);  // 댓글 수정
            log.info("Comment updated successfully: {}", commentNo);
        } catch (Exception e) {
            log.error("Error updating comment: {}", commentNo, e);
            return "redirect:/comments/qna/detail/" + qnaNo + "?error=true";  // 에러 발생 시 에러를 전달하며 상세 페이지로 리다이렉트
        }
        return "redirect:/comments/qna/detail/" + qnaNo;  // 수정 후 QnA 상세 페이지로 리다이렉트
    }

    // 특정 댓글을 삭제하는 메서드
    @PostMapping("/delete/{commentNo}")
    public String deleteComment(@PathVariable("commentNo") Long commentNo, @RequestParam("qnaNo") Long qnaNo) {
        commentService.deleteComment(commentNo);  // 댓글 삭제

        // QnA에 남아있는 댓글이 있는지 확인
        boolean hasComments = commentService.qnaHasComments(qnaNo);

        // 댓글이 없으면 QnA의 상태를 false로 업데이트
        if (!hasComments) {
            qnaService.updateQnaStatus(qnaNo, false);
        }

        return "redirect:/user/qna/detail/" + qnaNo;  // 삭제 후 QnA 상세 페이지로 리다이렉트
    }
}
