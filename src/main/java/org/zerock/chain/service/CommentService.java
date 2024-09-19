package org.zerock.chain.service;


import org.zerock.chain.dto.CommentDTO;
import org.zerock.chain.dto.CommentRequestDTO;

import java.util.List;

public interface CommentService {

    List<CommentDTO> getCommentsByQnaNo(Long qnaNo);
    // 특정 QnA 번호에 해당하는 모든 댓글을 조회합니다.

    CommentDTO addComment(CommentRequestDTO commentRequestDTO);
    // 새로운 댓글을 추가합니다.

    void deleteComment(Long commentNo);
    // 특정 댓글을 삭제합니다.

    void deleteCommentsByQnaNo(Long qnaNo);
    // 특정 QnA 번호에 해당하는 모든 댓글을 삭제합니다.

    CommentDTO updateComment(Long commentNo, CommentRequestDTO commentRequestDTO);
    // 특정 댓글을 수정합니다.

    boolean qnaHasComments(Long qnaNo);
    // 특정 QnA에 댓글이 있는지 여부를 확인합니다.
}
