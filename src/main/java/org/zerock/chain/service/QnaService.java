package org.zerock.chain.service;


import org.zerock.chain.dto.QnaDTO;
import org.zerock.chain.dto.QnaRequestDTO;

import java.util.List;

public interface QnaService {

    List<QnaDTO> getAllQnas(); // 모든 질문 가져오기
    QnaDTO getQnaById(Long qnaNo); // 특정 질문 가져오기
    QnaDTO createQna(QnaRequestDTO qnaRequestDTO); // 질문 등록
    QnaDTO updateQna(Long qnaNo, QnaRequestDTO qnaRequestDTO); // 질문 수정
    void deleteQna(Long qnaNo); // 질문 삭제
    void updateQnaStatus(Long qnaNo, boolean qnaStatus); // QnA 상태 업데이트

}
