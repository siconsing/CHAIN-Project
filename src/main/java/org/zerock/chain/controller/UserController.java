package org.zerock.chain.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zerock.chain.dto.*;
import org.zerock.chain.service.*;
import org.zerock.chain.model.Notification;
import org.zerock.chain.model.SystemNotification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@Log4j2
public class UserController {

    private final FavoriteQnaService favoriteQnaService;  // 즐겨찾기 QnA 서비스 의존성 주입
    private final QnaService qnaService;  // QnA 서비스 의존성 주입
    private final CommentService commentService;  // 댓글 서비스 의존성 주입
    private final NotificationService notificationService;  // 알림 서비스 의존성 주입
    private final SystemNotificationService systemNotificationService;  // 시스템 알림 서비스 의존성 주입
    private final EmployeeService employeeService;  // 사원 서비스 의존성 주입
    private final AttendanceRecordService attendanceRecordService;
    private final MonthlyAttendanceSummaryService monthlyAttendanceSummaryService;

    @Autowired
    public UserController(FavoriteQnaService favoriteQnaService, QnaService qnaService, CommentService commentService, NotificationService notificationService, SystemNotificationService systemNotificationService, EmployeeService employeeService, AttendanceRecordService attendanceRecordService, MonthlyAttendanceSummaryService monthlyAttendanceSummaryService) {
        this.favoriteQnaService = favoriteQnaService;
        this.qnaService = qnaService;
        this.commentService = commentService;
        this.notificationService = notificationService;
        this.systemNotificationService = systemNotificationService;
        this.employeeService = employeeService;
        this.attendanceRecordService = attendanceRecordService;
        this.monthlyAttendanceSummaryService = monthlyAttendanceSummaryService;
    }

    @PostMapping("/update-notification-setting")
    public ResponseEntity<?> updateNotificationSetting(@RequestBody Map<String, Object> payload) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        Long empNo = (Long) session.getAttribute("empNo");

        String notificationType = (String) payload.get("notificationType");
        Boolean enabled = (Boolean) payload.get("enabled");

        notificationService.updateNotificationSettingByType(empNo, notificationType, enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/qna/detail/{qnaNo}")
    public String detailQna(@PathVariable("qnaNo") Long qnaNo, Model model) {
        QnaDTO qna = qnaService.getQnaById(qnaNo);
        List<CommentDTO> comments = commentService.getCommentsByQnaNo(qnaNo);
        model.addAttribute("qna", qna);
        model.addAttribute("comments", comments);
        return "user/qaDetail";
    }


    @GetMapping("/mypage")
    public String getMypage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String empNo = authentication.getName();

        // 사원 번호가 null일 때 처리
        if (empNo == null) {
            model.addAttribute("employeeError", "로그인 정보가 없습니다.");
            // error 페이지로 이동하지 않고, 마이페이지를 렌더링할 수 있도록 처리
            return "user/mypage";
        }

        try {
            // 사원 정보 가져오기
            EmployeeDTO employee = employeeService.getEmployeeById(Long.parseLong(empNo));
            model.addAttribute("employee", employee);

            // 현재 날짜 가져오기
            LocalDate currentDate = LocalDate.now();

            // 월별 근태 요약 정보 가져오기
            List<MonthlyAttendanceSummaryDTO> monthlySummaries = monthlyAttendanceSummaryService.getSummariesByYearAndMonth(currentDate.getYear(), currentDate.getMonthValue());
            MonthlyAttendanceSummaryDTO currentMonthSummary = monthlySummaries.stream().findFirst().orElse(null);
            model.addAttribute("monthlyAttendanceSummary", currentMonthSummary);

            // 출퇴근 기록 가져오기
            AttendanceRecordDTO attendance = attendanceRecordService.getAttendanceRecordByDateAndEmpNo(currentDate, Long.parseLong(empNo));

            // 출퇴근 기록이 null인 경우 처리
            if (attendance == null) {
                model.addAttribute("dailyAttendanceMessage", "오늘의 출퇴근 기록이 없습니다.");
            } else {
                model.addAttribute("dailyAttendance", attendance);
            }

        } catch (NumberFormatException e) {
            // 사원 번호가 잘못된 경우
            model.addAttribute("employeeError", "유효하지 않은 사원 번호입니다.");
            log.error("유효하지 않은 사원 번호: {}", empNo);
            // error 페이지로 이동하지 않고 마이페이지를 렌더링
        } catch (Exception e) {
            // 기타 예외 처리
            model.addAttribute("employeeError", "사원 정보를 찾을 수 없습니다.");
            log.error("사원 정보 조회 실패: {}", e.getMessage());
            // error 페이지로 이동하지 않고 마이페이지를 렌더링
        }

        // 정상적으로 마이페이지로 이동
        return "user/mypage";
    }





    @GetMapping("/setting")
    public String userSetting(HttpSession session, Model model) {
        Long empNo = (Long) session.getAttribute("empNo");
        if (empNo == null) {
            return "redirect:/login";
        }

        EmployeeDTO employee = employeeService.getEmployeeById(empNo);
        model.addAttribute("employee", employee);
        return "user/setting";
    }

    @PostMapping("/update")
    public String updateEmployee(@RequestParam("lastName") String lastName,
                                 @RequestParam("firstName") String firstName,
                                 @RequestParam("address") String address,
                                 @RequestParam("phone") String phone,
                                 @RequestParam("email") String email,
                                 HttpSession session,
                                 Model model) {

        Long empNo = (Long) session.getAttribute("empNo");
        EmployeeDTO existingEmployee = employeeService.getEmployeeById(empNo);

        existingEmployee.setLastName(lastName);
        existingEmployee.setFirstName(firstName);
        existingEmployee.setAddr(address);
        existingEmployee.setPhoneNum(phone);
        existingEmployee.setEmail(email);

        employeeService.updateEmployee(empNo, existingEmployee);
        model.addAttribute("employee", existingEmployee);
        return "redirect:/user/setting";
    }

    @GetMapping("/Q&A")
    public String getAllQnasAndFaqs(Model model) {
        List<QnaDTO> qnaList = qnaService.getAllQnas();
        List<FavoriteQnaDTO> faqList = favoriteQnaService.getAllFAQs();

        qnaList.sort(Comparator.comparing(QnaDTO::getQnaUploadDate).reversed());
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("faqList", faqList);
        return "user/Q&A";
    }

    @PostMapping("/faq/add")
    public String registerFAQ(@RequestParam String faqName, @RequestParam String faqContent) {
        FavoriteQnaRequestDTO requestDTO = new FavoriteQnaRequestDTO();
        requestDTO.setFaqName(faqName);
        requestDTO.setFaqContent(faqContent);
        favoriteQnaService.createFAQ(requestDTO);
        return "redirect:/user/Q&A";
    }

    @PostMapping("/faq/edit")
    public String editFaq(@RequestParam("faqNo") Long faqNo,
                          @RequestParam("faqName") String faqName,
                          @RequestParam("faqContent") String faqContent) {
        favoriteQnaService.updateFaq(faqNo, faqName, faqContent);
        return "redirect:/user/Q&A";
    }

    @PostMapping("/faq/delete")
    public String deleteFaq(@RequestParam("faqNo") Long faqNo) {
        favoriteQnaService.deleteFaq(faqNo);
        return "redirect:/user/Q&A";
    }

    @GetMapping("/qna/register")
    public String showQnaRegisterPage(Model model) {
        model.addAttribute("qnaRequestDTO", new QnaRequestDTO());
        return "user/qaRegister";
    }

    @PostMapping("/qna/register")
    public String registerQna(@ModelAttribute QnaRequestDTO qnaRequestDTO) {
        QnaDTO createdQna = qnaService.createQna(qnaRequestDTO);
        return "redirect:/user/qna/detail/" + createdQna.getQnaNo();
    }

    @GetMapping("/qna/edit/{qnaNo}")
    public String editQnaPage(@PathVariable("qnaNo") Long qnaNo, Model model) {
        QnaDTO qna = qnaService.getQnaById(qnaNo);
        model.addAttribute("qna", qna);
        return "user/qaModify";
    }

    @PostMapping("/qna/edit/{qnaNo}")
    public String updateQna(@PathVariable("qnaNo") Long qnaNo, @ModelAttribute QnaRequestDTO qnaRequestDTO) {
        qnaService.updateQna(qnaNo, qnaRequestDTO);
        return "redirect:/user/qna/detail/" + qnaNo;
    }

    @DeleteMapping("/qna/delete/{qnaNo}")
    @ResponseBody
    public String deleteQna(@PathVariable("qnaNo") Long qnaNo) {
        qnaService.deleteQna(qnaNo);
        return "redirect:/user/Q&A";
    }

    @PostMapping("/systemNotification")
    public String createSystemNotification(@RequestParam("systemCategory") String systemCategory,
                                           @RequestParam("systemTitle") String systemTitle,
                                           @RequestParam("systemContent") String systemContent) {

        SystemNotification systemNotification = new SystemNotification();
        systemNotification.setSystemCategory(systemCategory);
        systemNotification.setSystemTitle(systemTitle);
        systemNotification.setSystemContent(systemContent);
        systemNotification.setSystemUploadDate(LocalDateTime.now());
        systemNotification.setRead(false);

        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();
        for (EmployeeDTO employee : allEmployees) {
            SystemNotification individualNotification = new SystemNotification();
            individualNotification.setSystemCategory(systemCategory);
            individualNotification.setSystemTitle(systemTitle);
            individualNotification.setSystemContent(systemContent);
            individualNotification.setSystemUploadDate(LocalDateTime.now());
            individualNotification.setRead(false);
            individualNotification.setEmpNo(employee.getEmpNo());

            systemNotificationService.saveSystemNotification(individualNotification);
        }

        return "redirect:/user/alarm";
    }

    @GetMapping("/alarm")
    public String userAlarm(Model model) {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        Long empNo = (Long) session.getAttribute("empNo");

        List<Notification> allNotifications = notificationService.getAllNotifications(empNo);  // 모든 알림 조회
        List<Notification> projectNotifications = notificationService.getNotificationsByType(empNo, "프로젝트");  // 프로젝트 알림 조회
        List<Notification> noticeNotifications = notificationService.getNotificationsByType(empNo, "공지사항");  // 공지사항 알림 조회
        List<Notification> reportNotifications = notificationService.getNotificationsByType(empNo, "업무보고서");  // 업무보고서 알림 조회
        List<Notification> chartNotifications = notificationService.getNotificationsByType(empNo, "차트");  // 차트 알림 조회
        List<Notification> approvalNotifications = notificationService.getNotificationsByType(empNo, "전자결재");  // 전자결재 알림 조회(영민 추가)
        List<Notification> accountNotifications = notificationService.getNotificationsByType(empNo, "계정");	  // 계정 알림 조회
        List<SystemNotification> systemNotifications = systemNotificationService.getAllSystemNotifications(empNo);  // 시스템 알림 조회

        allNotifications.sort(Comparator.comparing(Notification::getNotificationDate).reversed());
        systemNotifications.sort(Comparator.comparing(SystemNotification::getSystemUploadDate).reversed());

        model.addAttribute("allNotifications", allNotifications != null ? allNotifications : List.of());  // 모든 알림을 모델에 추가
        model.addAttribute("projectNotifications", projectNotifications != null ? projectNotifications : List.of());  // 프로젝트 알림을 모델에 추가
        model.addAttribute("noticeNotifications", noticeNotifications != null ? noticeNotifications : List.of());  // 공지사항 알림을 모델에 추가
        model.addAttribute("reportNotifications", reportNotifications != null ? reportNotifications : List.of());  // 업무보고서 알림을 모델에 추가
        model.addAttribute("chartNotifications", chartNotifications != null ? chartNotifications : List.of());  // 차트 알림을 모델에 추가
        model.addAttribute("approvalNotifications", approvalNotifications != null ? approvalNotifications : List.of());  // 전자결재 알림을 모델에 추가(영민 추가)
        model.addAttribute("accountNotifications", accountNotifications != null ? accountNotifications : List.of());  // 인사관리 알림을 모델에 추가
        model.addAttribute("systemNotifications", systemNotifications != null ? systemNotifications : List.of());  // 시스템 알림을 모델에 추가

        return "user/alarm";
    }

    @GetMapping("/alarm/read/notification/{notificationNo}")
    public String readNotificationAndRedirect(@PathVariable("notificationNo") Long notificationNo) {
        Notification notification = notificationService.getNotificationById(notificationNo);
        notificationService.markAsRead(notificationNo);
        String redirectUrl = notification.getRedirectUrl();
        return "redirect:" + redirectUrl;
    }

    // 알림 클릭 시 읽음 상태로 변경하고, 해당 페이지로 리다이렉트(영민 추가)
    @GetMapping("/alarm/read/{notificationNo}")
    public String yeongReadNotificationAndRedirect(@PathVariable Long notificationNo) {
        Notification notification = notificationService.getNotificationById(notificationNo);  // 특정 알림 번호로 알림 조회
        notificationService.markAsRead(notificationNo);  // 알림 읽음 상태로 업데이트

        String redirectUrl = notification.getRedirectUrl();  // 리다이렉트 URL 가져오기
        return "redirect:" + redirectUrl;  // 해당 URL로 리다이렉트
    }

    // 시스템 알림 클릭 시 읽음 상태로 변경
    @GetMapping("/alarm/read/system/{systemNo}")
    public String readSystemAndRedirect(@PathVariable("systemNo") Long systemNo) {
        SystemNotification systemNotification = systemNotificationService.getSystemNotificationById(systemNo);
        systemNotificationService.markAsReadSystem(systemNo);
        return "redirect:/user/alarm";
    }

    @PostMapping("/alarm/deleteAll")
    public String deleteAllNotifications() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        Long empNo = (Long) session.getAttribute("empNo");

        notificationService.deleteAllNotifications(empNo);
        systemNotificationService.deleteAllSystemNotifications();

        return "redirect:/user/alarm";
    }

    @PostMapping("/alarm/delete/{notificationNo}")
    public String deleteNotification(@PathVariable("notificationNo") Long notificationNo) {
        notificationService.deleteNotification(notificationNo);
        return "redirect:/user/alarm";
    }

    @PostMapping("/alarm/deleteRead")
    public String deleteReadNotifications() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attr.getRequest().getSession();
        Long empNo = (Long) session.getAttribute("empNo");

        notificationService.deleteReadNotifications(empNo);
        systemNotificationService.deleteReadSystemNotification(empNo);

        return "redirect:/user/alarm";
    }

    @PostMapping("/systemNotification/delete/{systemNo}")
    public String deleteSystemNotification(@PathVariable("systemNo") Long systemNo) {
        systemNotificationService.deleteSystemNotification(systemNo);
        return "redirect:/user/alarm";
    }
}
