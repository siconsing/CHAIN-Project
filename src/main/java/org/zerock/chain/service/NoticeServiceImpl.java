package org.zerock.chain.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.chain.dto.NoticeDTO;
import org.zerock.chain.dto.NoticeRequestDTO;
import org.zerock.chain.model.Notice;
import org.zerock.chain.repository.NoticeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final ModelMapper modelMapper;

    public NoticeServiceImpl(NoticeRepository noticeRepository, ModelMapper modelMapper) {
        this.noticeRepository = noticeRepository;
        this.modelMapper = modelMapper;
    }

    // 공지사항 전체 목록 조회 시, noticePinned 값을 검증 및 업데이트
    @Override
    public List<NoticeDTO> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::updatePinnedStatusAndConvertToDTO)
                .collect(Collectors.toList());
    }

    // 개별 공지사항 상세 조회 시, noticePinned 값을 검증 및 업데이트
    @Override
    public NoticeDTO getNoticeById(Long noticeNo) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new RuntimeException("Notice not found with id: " + noticeNo));
        return updatePinnedStatusAndConvertToDTO(notice);
    }

    // 새로운 공지사항 생성
    @Override
    public NoticeDTO createNotice(NoticeRequestDTO noticeRequestDTO) {
        Notice notice = modelMapper.map(noticeRequestDTO, Notice.class);
        notice = noticeRepository.save(notice);
        return modelMapper.map(notice, NoticeDTO.class);
    }

    // 기존 공지사항 수정
    @Override
    public NoticeDTO updateNotice(Long noticeNo, NoticeRequestDTO noticeRequestDTO) {
        Notice notice = noticeRepository.findById(noticeNo)
                .orElseThrow(() -> new RuntimeException("Notice not found with id: " + noticeNo));

        modelMapper.map(noticeRequestDTO, notice);
        notice = noticeRepository.save(notice);
        return modelMapper.map(notice, NoticeDTO.class);
    }

    // 공지사항 삭제
    @Override
    public void deleteNotice(Long noticeNo) {
        noticeRepository.deleteById(noticeNo);
    }

    // Notice 엔티티의 noticePinned 값을 현재 날짜에 따라 업데이트하고 DTO로 변환
    private NoticeDTO updatePinnedStatusAndConvertToDTO(Notice notice) {
        if (notice.getNoticePinnedDate() != null) {
            if (notice.getNoticePinnedDate().isBefore(LocalDate.now())) {
                notice.setNoticePinned(false);  // 고정 기간이 지났다면 고정 해제
            } else {
                notice.setNoticePinned(true);   // 고정 기간이 아직 남아 있다면 고정
            }
        }

        return modelMapper.map(notice, NoticeDTO.class);
    }
}
