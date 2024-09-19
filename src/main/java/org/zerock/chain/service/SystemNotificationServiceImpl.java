package org.zerock.chain.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.model.SystemNotification;
import org.zerock.chain.repository.SystemNotificationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemNotificationServiceImpl  extends BaseService<SystemNotification> implements SystemNotificationService {

    private final SystemNotificationRepository systemNotificationRepository;

    @Autowired
    public SystemNotificationServiceImpl(SystemNotificationRepository systemNotificationRepository) {
        this.systemNotificationRepository = systemNotificationRepository;
    }

    @Override   // 시스템 알림 등록
    public void saveSystemNotification(SystemNotification systemNotification) {
        systemNotificationRepository.save(systemNotification);
    }

    @Override   // 모든 시스템 알림 조회
    public List<SystemNotification> getAllSystemNotifications(Long empNo) {
        return getItemsByEmpNo(empNo, this::getAllItemsByEmpNo).stream()
                .sorted(Comparator.comparing(SystemNotification::getSystemUploadDate).reversed()) // 최신순으로 정렬
                .collect(Collectors.toList());
    }

    @Override // 모든 시스템 알림 삭제
    @Transactional
    public void deleteAllSystemNotifications() {
        systemNotificationRepository.deleteAll();
    }

    @Override // 개별 시스템 알림 삭제
    public void deleteSystemNotification(Long systemNo) {
        systemNotificationRepository.deleteById(systemNo);
    }

    // 특정 알림을 읽음 상태로 변경함
    @Override
    @Transactional
    public void markAsReadSystem(Long systemNo) {
        Optional<SystemNotification> notificationOpt = systemNotificationRepository.findById(systemNo);
        if (notificationOpt.isPresent()) {
            SystemNotification systemNotification = notificationOpt.get();
            systemNotification.setRead(true);
            systemNotificationRepository.save(systemNotification);
        }
    }
    // 특정 알림을 ID로 조회함
    @Override
    public SystemNotification getSystemNotificationById(Long systemNo) {
        return systemNotificationRepository.findById(systemNo)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    // 새로운 메서드: 특정 사원의 읽은 알림을 모두 삭제함
    @Override
    @Transactional
    public void deleteReadSystemNotification(Long empNo) {
        // systemNotificationRepository 새로운 메서드를 호출하여 읽은 알림을 삭제
        systemNotificationRepository.deleteByEmpNoAndIsRead(empNo);
    }

    // 특정 사원번호(empNo)로 모든 알림을 가져옴
    @Override
    protected List<SystemNotification> getAllItemsByEmpNo(Long empNo) {
        return systemNotificationRepository.findByEmpNo(empNo);
    }
}