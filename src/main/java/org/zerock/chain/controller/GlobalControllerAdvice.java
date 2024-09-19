package org.zerock.chain.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zerock.chain.model.Notification;
import org.zerock.chain.model.SystemNotification;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.zerock.chain.service.NotificationService;
import org.zerock.chain.service.SystemNotificationService;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice  // 모든 컨트롤러에 대해 공통적인 기능을 제공하는 클래스 선언
public class GlobalControllerAdvice {

    @Autowired  // NotificationService를 주입받아 사용
    private NotificationService notificationService;

    @Autowired  // SystemNotificationService를 주입받아 사용
    private SystemNotificationService systemNotificationService;

    @ModelAttribute("allNotifications")  // "allNotifications"라는 이름으로 모델 데이터를 설정
    public List<Notification> populateNotifications(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {  // 사용자인증 확인
            // 세션에서 사원번호(empNo) 가져오기
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession();  // 현재 요청의 세션 객체를 가져옴
            Long empNo = (Long) session.getAttribute("empNo");  // 세션에 저장된 사원번호를 가져옴

            if (empNo != null) {
                // 사원번호를 통해 모든 알림 가져오기
                return notificationService.getAllNotifications(empNo);  // 해당 사원번호에 대한 모든 알림 반환
            }
        }
        // 인증되지 않았거나 사원번호가 없는 경우 빈 리스트 반환
        return new ArrayList<>();  // 인증 실패 시 또는 사원번호가 없을 때 빈 리스트 반환
    }

    @ModelAttribute("systemNotifications")  // "systemNotifications"라는 이름으로 모델 데이터를 설정
    public List<SystemNotification> populateSystemNotifications(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {  // 사용자인증 확인
            // 세션에서 사원번호(empNo) 가져오기
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession();  // 현재 요청의 세션 객체를 가져옴
            Long empNo = (Long) session.getAttribute("empNo");  // 세션에 저장된 사원번호를 가져옴

            if (empNo != null) {
                // 사원번호를 통해 해당 사원의 시스템 알림 가져오기
                return systemNotificationService.getAllSystemNotifications(empNo);  // 해당 사원번호에 대한 시스템 알림 반환
            }
        }
        // 인증되지 않았거나 사원번호가 없는 경우 빈 리스트 반환
        return new ArrayList<>();  // 인증 실패 시 또는 사원번호가 없을 때 빈 리스트 반환
    }

    @ModelAttribute("firstName")
    public String populateFirstName(HttpSession session) {
        return (String) session.getAttribute("firstName");  // 세션에서 firstName 가져와서 반환
    }

    @ModelAttribute("lastName")
    public String populateLastName(HttpSession session) {
        return (String) session.getAttribute("lastName");  // 세션에서 lastName 가져와서 반환
    }

    @ModelAttribute("rankName")
    public String populateRankName(HttpSession session) {
        return (String) session.getAttribute("rankName");  // 세션에서 rankName 가져와서 반환
    }

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
    }

    @ModelAttribute("departmentName")
    public String populateDepartmentName(HttpSession session) {
        return (String) session.getAttribute("departmentName"); // 세션에서 부서 이름 가져오기
    }

}
