package org.zerock.chain.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.QnaDTO;
import org.zerock.chain.dto.QnaRequestDTO;
import org.zerock.chain.model.Qna;
import org.zerock.chain.repository.CommentRepository;
import org.zerock.chain.repository.QnaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class QnaServiceImpl implements QnaService {

    private final ModelMapper modelMapper;
    private final QnaRepository qnaRepository;
    private final CommentRepository commentRepository;

    @Override   // 문의글 전체 조회
    public List<QnaDTO> getAllQnas() {
        List<Qna> qnas = qnaRepository.findAll();
        return qnas.stream()
                .map(qna -> modelMapper.map(qna, QnaDTO.class))
                .collect(Collectors.toList());
    }

    @Override   // 특정 문의글 조회 (수정조회)
    public QnaDTO getQnaById(Long qnaNo) {
        Qna qna = qnaRepository.findById(qnaNo).orElseThrow();
        return modelMapper.map(qna, QnaDTO.class);
    }

    @Override   // 문의글 생성 등록
    public QnaDTO createQna(QnaRequestDTO qnaRequestDTO) {
        Qna qna = modelMapper.map(qnaRequestDTO, Qna.class);
        Qna savedQna = qnaRepository.save(qna);
        return modelMapper.map(savedQna, QnaDTO.class);
    }

    @Override   // 문의글 수정 등록
    public QnaDTO updateQna(Long qnaNo, QnaRequestDTO qnaRequestDTO) {
        Optional<Qna> result = qnaRepository.findById(qnaNo);
        Qna qna = result.orElseThrow();
        modelMapper.map(qnaRequestDTO, qna);
        qnaRepository.save(qna);
        return modelMapper.map(qna, QnaDTO.class); // 수정된 부분 반환
    }


     // 문의글 삭제
    @Override
    @Transactional
    public void deleteQna(Long qnaNo) {
        // Q&A에 연관된 댓글 삭제
        commentRepository.deleteByCommentQnaQnaNo(qnaNo);

        // Q&A 삭제
        qnaRepository.deleteById(qnaNo);
    }

    // 답변상태 업데이트
    @Override
    public void updateQnaStatus(Long qnaNo, boolean qnaStatus) {
        // QnA의 상태를 업데이트하는 로직
        Qna qna = qnaRepository.findById(qnaNo)
                .orElseThrow(() -> new RuntimeException("QnA not found with id " + qnaNo));
        qna.setQnaStatus(qnaStatus);
        qnaRepository.save(qna);
    }
}
