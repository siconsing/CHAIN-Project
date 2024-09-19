package org.zerock.chain.service;


import org.zerock.chain.dto.NoticeDTO;
import org.zerock.chain.dto.NoticeRequestDTO;

import java.util.List;

public interface NoticeService {

    // 공지사항 전체 목록 조회
    List<NoticeDTO> getAllNotices();
    // 개별 공지사항 상세 조회
    NoticeDTO getNoticeById(Long noticeNo);
    // 새로운 공지사항 생성
    NoticeDTO createNotice(NoticeRequestDTO noticeRequestDTO);
    // 기존 공지사항 수정
    NoticeDTO updateNotice(Long noticeNo, NoticeRequestDTO noticeRequestDTO);
    // 공지사항 삭제
    void deleteNotice(Long noticeNo);
}
