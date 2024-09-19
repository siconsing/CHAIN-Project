package org.zerock.chain.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.model.Approval;
import org.zerock.chain.model.Documents;
import org.zerock.chain.repository.ApprovalRepository;
import org.zerock.chain.repository.DocumentsRepository;
import org.zerock.chain.model.Notification;
import org.zerock.chain.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Log4j2
@Service
public class NotificationServiceImpl extends BaseService<Notification> implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired  // (영민)이 추가
    private ApprovalRepository approvalRepository;
    @Autowired  // (영민)이 추가
    private DocumentsRepository documentsRepository;

    // 사원번호와 알림 타입으로 알림 상태 업데이트
    @Override
    @Transactional
    public void updateNotificationSettingByType(Long empNo, String notificationType, Boolean enabled) {
        notificationRepository.updateEnabledByEmpNoAndType(empNo, notificationType, enabled);
    }

    // 특정 사원번호(empNo)로 모든 알림을 가져옴
    @Override
    protected List<Notification> getAllItemsByEmpNo(Long empNo) {
        return notificationRepository.findByEmpNo(empNo);
    }

    // 특정 타입의 알림을 가져옴
    @Override
    public List<Notification> getNotificationsByType(Long empNo, String type) {
        return getItemsByEmpNo(empNo, empNoParam -> notificationRepository.findByEmpNoAndNotificationType(empNoParam, type));
    }

    // 특정 사원의 모든 알림을 최신순으로 가져옴
    @Override
    public List<Notification> getAllNotifications(Long empNo) {
        return getItemsByEmpNo(empNo, this::getAllItemsByEmpNo).stream()
                .sorted(Comparator.comparing(Notification::getNotificationDate).reversed()) // 최신순으로 정렬
                .collect(Collectors.toList());
    }

    // 특정 사원의 모든 알림을 삭제함
    @Override
    @Transactional
    public void deleteAllNotifications(Long empNo) {
        notificationRepository.deleteByEmpNo(empNo);
    }

    // 특정 알림을 삭제함
    @Override
    public void deleteNotification(Long notificationNo) {
        notificationRepository.deleteById(notificationNo);
    }

    // 특정 알림을 읽음 상태로 변경함
    @Override
    @Transactional
    public void markAsRead(Long notificationNo) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationNo);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    // 특정 알림을 ID로 조회함
    @Override
    public Notification getNotificationById(Long notificationNo) {
        return notificationRepository.findById(notificationNo)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    // 새로운 메서드: 특정 사원의 읽은 알림을 모두 삭제함
    @Override
    @Transactional
    public void deleteReadNotifications(Long empNo) {
        // NotificationRepository의 새로운 메서드를 호출하여 읽은 알림을 삭제
        notificationRepository.deleteByEmpNoAndIsRead(empNo);
    }

    @Override  // 전자결재 알림 생성 메서드 (영민)
    public void createApprovalNotification(int docNo, String docTitle, String senderName, String docStatus, String withdraw) {
        // 결재자 사원의 번호를 가져옴
        List<Long> empNos = approvalRepository.findEmpNosByDocNo(docNo);
        // 참조자 사원의 번호를 가져옴
        List<Long> refEmpNos = approvalRepository.findRefEmpNosByDocNo(docNo);

        // 기안자(작성자)의 사원 번호를 가져옴
        Documents document = documentsRepository.findById(docNo)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        Long senderEmpNo = document.getLoggedInEmpNo();  // logged_in_emp_no에서 기안자의 사원 번호 가져오기

        // 모든 관련 사원의 번호를 하나의 리스트로 병합
        Set<Long> allEmpNos = new HashSet<>();
        allEmpNos.addAll(empNos);
        allEmpNos.addAll(refEmpNos);
        allEmpNos.add(senderEmpNo);  // 기안자도 추가

        // 각 사원에게 알림 생성
        for (Long empNoLong : allEmpNos) {
            if (empNoLong != null) {

                String message;

                if ("true".equals(withdraw)) {
                    // 철회된 문서에 대한 맞춤형 알림 메시지
                    message = String.format("%s 님이 요청한 %d번 문서 '%s'이(가) 철회되었습니다.", senderName, docNo, docTitle);
                } else if (isCurrentApprover(docNo, empNoLong)) {
                    // 현재 결재 차례인 사람에게 맞춤형 알림 메시지
                    message = String.format("%s 님이 요청한 %d번 문서 '%s'의 결재를 진행해주십시오.", senderName, docNo, docTitle);
                } else {
                    // 일반적인 결재 완료/반려 메시지
                    message = String.format("%s 님이 요청한 %d번 문서 '%s'의 현재 결재 상태는 %s입니다.", senderName, docNo, docTitle, docStatus);
                }

                Notification notification = new Notification();
                notification.setEmpNo(empNoLong);
                notification.setRead(false);
                notification.setNotificationDate(notification.getNotificationDate());
                notification.setNotificationMessage(message);
                notification.setNotificationType("전자결재");
                notification.setReferenceId(0L);

                notificationRepository.save(notification);
            }
        }
    }

    // (영민)이 추가
    private boolean isCurrentApprover(int docNo, Long empNo) {
        // 현재 사용자의 Approval 정보 조회
        Approval currentApproval = approvalRepository.findByDocumentsDocNoAndEmployeeEmpNo(docNo, empNo);

        if (currentApproval != null) {
            int currentOrder = currentApproval.getApprovalOrder();

            // 이전 결재자의 승인 여부 확인
            if (currentOrder > 1) {
                Approval previousApproval = approvalRepository.findByDocumentsDocNoAndApprovalOrder(docNo, currentOrder - 1);

                // 이전 결재자가 승인했을 때만 현재 결재자가 승인/반려 버튼을 볼 수 있음
                return previousApproval != null && "승인".equals(previousApproval.getApprovalStatus()) &&
                        "대기".equals(currentApproval.getApprovalStatus());
            } else {
                // 첫 번째 결재자라면 이전 결재자가 없으므로 바로 승인/반려 가능
                return "대기".equals(currentApproval.getApprovalStatus());
            }
        }
        return false;
    }

    @Override
    @Transactional
    public void createDepartmentAndRankChangeNotification(Long empNo, String oldDepartment, String newDepartment, String oldRank, String newRank) {
        String departmentChangeMessage = null;
        String rankChangeMessage = null;

        // 부서 변경 메시지 생성
        if (oldDepartment != null && !oldDepartment.equals(newDepartment)) {
            departmentChangeMessage = String.format("부서가 %s에서 %s로 변경되었습니다.", oldDepartment, newDepartment);
        }

        // 직급 변경 메시지 생성
        if (oldRank != null && !oldRank.equals(newRank)) {
            rankChangeMessage = String.format("직급이 %s에서 %s로 변경되었습니다.", oldRank, newRank);
        }

        // 부서 변경 알림 생성
        if (departmentChangeMessage != null) {
            Notification departmentNotification = new Notification();
            departmentNotification.setEmpNo(empNo);
            departmentNotification.setNotificationType("계정");
            departmentNotification.setNotificationMessage(departmentChangeMessage);
            departmentNotification.setNotificationDate(LocalDateTime.now());
            departmentNotification.setRead(false);
            departmentNotification.setEnabled(true);
            notificationRepository.save(departmentNotification);
        }

        // 직급 변경 알림 생성
        if (rankChangeMessage != null) {
            Notification rankNotification = new Notification();
            rankNotification.setEmpNo(empNo);
            rankNotification.setNotificationType("계정");
            rankNotification.setNotificationMessage(rankChangeMessage);
            rankNotification.setNotificationDate(LocalDateTime.now());
            rankNotification.setRead(false);
            rankNotification.setEnabled(true);
            notificationRepository.save(rankNotification);
        }
    }



}
