package org.zerock.chain.controller;

import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.chain.dto.NoticeDTO;
import org.zerock.chain.dto.NoticeRequestDTO;
import org.zerock.chain.service.NoticeService;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/notice")
@Log4j2
public class NoticeController {

    private final NoticeService noticeService;  // 공지사항 서비스 계층 의존성 주입

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;  // NoticeService 초기화
    }

    // 공지사항 전체 목록 조회
    @GetMapping("/list")
    public String getAllNotices(Model model) {
        List<NoticeDTO> notices = noticeService.getAllNotices();  // 모든 공지사항 조회

        // 고정값 순서와 최신순으로 정렬
        notices.sort(Comparator
                .comparing(NoticeDTO::getNoticePinned).reversed()  // 고정된 항목이 먼저 오도록 정렬
                .thenComparing(Comparator.comparing(NoticeDTO::getNoticeCreatedDate).reversed()));  // 그 다음 최신순으로 정렬

        model.addAttribute("notices", notices);  // 정렬된 공지사항 리스트를 모델에 추가
        return "notice/list";  // 공지사항 목록 페이지로 이동
    }

    // 개별 공지사항 상세 조회
    @GetMapping("/detail/{noticeNo}")
    public String getNoticeBynoticeNo(@PathVariable("noticeNo") Long noticeNo, Model model) {
        NoticeDTO notice = noticeService.getNoticeById(noticeNo);  // 개별 공지사항 조회
        model.addAttribute("notice", notice);  // 조회된 공지사항을 모델에 추가
        return "notice/detail";  // 공지사항 상세 페이지로 이동
    }

    // 공지사항 등록 페이지 표시
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("noticeRequestDTO", new NoticeRequestDTO());  // 빈 NoticeRequestDTO를 모델에 추가
        return "notice/register";  // 공지사항 등록 페이지로 이동
    }

    // 새 공지사항 생성
    @PostMapping("/register")
    public String createNotice(@Valid @ModelAttribute NoticeRequestDTO noticeRequestDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "notice/register";  // 입력값에 에러가 있을 경우 등록 페이지로 돌아감
        }
        NoticeDTO createdNotice = noticeService.createNotice(noticeRequestDTO);  // 새로운 공지사항 생성
        return "redirect:/notice/detail/" + createdNotice.getNoticeNo();  // 성공적으로 생성된 경우 상세 페이지로 리디렉션
    }

    // 공지사항 수정 페이지 표시
    @GetMapping("/modify/{noticeNo}")
    public String showModifyPage(@PathVariable("noticeNo") Long noticeNo, Model model, RedirectAttributes redirectAttributes) {
        NoticeDTO notice = noticeService.getNoticeById(noticeNo);  // 수정할 공지사항 조회
        if (notice == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항을 찾을 수 없습니다.");  // 공지사항을 찾을 수 없는 경우 오류 메시지 추가
            return "redirect:/notice/list";  // 목록 페이지로 리다이렉트
        }
        model.addAttribute("notice", notice);  // 수정할 공지사항을 모델에 추가
        return "notice/modify";  // 공지사항 수정 페이지로 이동
    }

    // 공지사항 수정
    @PostMapping("/modify/{noticeNo}")
    public String updateNotice(@PathVariable("noticeNo") Long noticeNo, @Valid @ModelAttribute NoticeRequestDTO noticeRequestDTO, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "notice/modify";  // 입력값에 에러가 있을 경우 수정 페이지로 돌아감
        }
        noticeService.updateNotice(noticeNo, noticeRequestDTO);  // 공지사항 수정 처리
        return "redirect:/notice/detail/" + noticeNo;  // 수정된 공지사항의 상세 페이지로 리다이렉트
    }

    // 공지사항 삭제
    @PostMapping("/delete/{noticeNo}")
    public String deleteNotice(@PathVariable("noticeNo") Long noticeNo, RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(noticeNo);  // 공지사항 삭제 처리
            redirectAttributes.addFlashAttribute("successMessage", "공지사항이 성공적으로 삭제되었습니다.");  // 성공 메시지 추가
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "공지사항 삭제에 실패했습니다.");  // 실패 메시지 추가
            log.error("Error deleting notice", e);  // 오류 로그 기록
        }
        return "redirect:/notice/list";  // 공지사항 목록 페이지로 리다이렉트
    }
}
