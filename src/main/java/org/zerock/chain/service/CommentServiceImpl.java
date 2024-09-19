package org.zerock.chain.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.CommentDTO;
import org.zerock.chain.dto.CommentRequestDTO;
import org.zerock.chain.model.Comment;
import org.zerock.chain.model.Qna;
import org.zerock.chain.repository.CommentRepository;
import org.zerock.chain.repository.QnaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;
    private final QnaRepository qnaRepository;


    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, QnaRepository qnaRepository, ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.qnaRepository = qnaRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    // 특정 QnA 번호에 해당하는 모든 댓글을 조회
    public List<CommentDTO> getCommentsByQnaNo(Long qnaNo) {
        return commentRepository.findByCommentQna_QnaNo(qnaNo)
                .stream()
                .map(comment -> modelMapper.map(comment, CommentDTO.class)) // 엔티티를 DTO로 변환
                .collect(Collectors.toList()); // 결과를 리스트로 수집
    }

    @Override
    public CommentDTO addComment(CommentRequestDTO commentRequestDTO) {
        // 댓글을 생성하고 저장
        Comment comment = modelMapper.map(commentRequestDTO, Comment.class);
        // QnA 번호로 QnA 객체를 조회
        Qna qna = qnaRepository.findById(commentRequestDTO.getQnaNo())
                .orElseThrow(() -> new RuntimeException("Qna not found"));

        // 댓글에 QnA 객체 설정
        comment.setCommentQna(qna);
        Comment savedComment = commentRepository.save(comment);

        // qnaStatus를 true로 설정하고 저장
        qna.setQnaStatus(true); // 수정된 부분
        qnaRepository.save(qna);

        // 저장된 댓글을 DTO로 변환하여 반환
        return modelMapper.map(savedComment, CommentDTO.class);
    }

    @Override
    public CommentDTO updateComment(Long commentNo, CommentRequestDTO commentRequestDTO) {
        Comment comment = commentRepository.findById(commentNo)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setCommentName(commentRequestDTO.getCommentName());
        comment.setCommentContent(commentRequestDTO.getCommentContent());

        Comment updatedComment = commentRepository.save(comment);

        return modelMapper.map(updatedComment, CommentDTO.class);
    }

    @Override
    public void deleteComment(Long commentNo) {
        Comment comment = commentRepository.findById(commentNo)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 댓글 삭제
        commentRepository.deleteById(commentNo);

        // 삭제 후 해당 QnA의 댓글 여부를 확인하여 업데이트
        Qna qna = comment.getCommentQna();
        boolean hasComments = commentRepository.countByCommentQna(qna) > 0;
        qna.setQnaStatus(hasComments);
        qnaRepository.save(qna);
    }

    @Override  // 특정 QnA 번호에 해당하는 모든 댓글을 삭제
    public void deleteCommentsByQnaNo(Long qnaNo) {
        commentRepository.deleteByCommentQnaQnaNo(qnaNo);
    }

    @Override
    public boolean qnaHasComments(Long qnaNo) {
        Qna qna = qnaRepository.findById(qnaNo).orElseThrow(() -> new IllegalArgumentException("Invalid QnA ID"));
        return commentRepository.countByCommentQna(qna) > 0;
    }


}
