package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.chain.model.Comment;
import org.zerock.chain.model.Qna;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 QnA 번호에 대한 모든 댓글을 가져오는 메서드
    List<Comment> findByCommentQna_QnaNo(Long qnaNo);

    // 특정 QnA 번호와 댓글 작성자에 따라 댓글을 필터링하는 메서드
    List<Comment> findByCommentQna_QnaNoAndCommentAuthor(Long qnaNo, String author);

    // Q&A 번호를 기준으로 모든 댓글 삭제
    void deleteByCommentQnaQnaNo(Long qnaNo);

    // QnA 객체에 해당하는 댓글 개수를 세는 메서드
    int countByCommentQna(Qna commentQna);
}
