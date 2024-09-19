package org.zerock.chain.service;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.BoardDTO;
import org.zerock.chain.dto.BoardRequestDTO;
import org.zerock.chain.model.Board;
import org.zerock.chain.repository.BoardRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    private final ModelMapper modelMapper; // ModelMapper를 통한 객체 변환을 위한 필드
    private final BoardRepository boardRepository; // 게시글 관련 DB 작업을 위한 레포지토리 필드

    public BoardServiceImpl(ModelMapper modelMapper, BoardRepository boardRepository) {
        this.modelMapper = modelMapper; // ModelMapper 초기화
        this.boardRepository = boardRepository; // BoardRepository 초기화
    }

    @Override // 전체 목록 조회
    public List<BoardDTO> getAllBoards() {
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "boardUploadDate")).stream()
                .map(board -> modelMapper.map(board, BoardDTO.class)) // Board 엔티티를 BoardDTO로 변환
                .collect(Collectors.toList()); // 변환된 DTO 리스트 반환
    }

    @Override // 개별 게시글 조회
    public BoardDTO getBoardById(Long boardNo) {
        Board board = boardRepository.findById(boardNo).orElseThrow(); // ID로 게시글 조회, 없을 경우 예외 발생
        return modelMapper.map(board, BoardDTO.class); // 조회된 게시글을 DTO로 변환하여 반환
    }

    @Override // 게시글 생성
    public BoardDTO createBoard(BoardRequestDTO boardRequestDTO) {
        Board board = modelMapper.map(boardRequestDTO, Board.class); // 요청 DTO를 Board 엔티티로 변환
        board = boardRepository.save(board); // 변환된 엔티티를 DB에 저장
        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class); // 저장된 엔티티를 DTO로 변환
        return boardDTO; // 생성된 게시글 DTO 반환
    }

    @Override // 게시글 수정
    public BoardDTO updateBoard(Long boardNo, BoardRequestDTO boardRequestDTO) {
        // 로그 추가
        System.out.println("Original boardFiles: " + boardRequestDTO.getBoardFiles());
        System.out.println("Original boardLocation: " + boardRequestDTO.getBoardLocation());

        // 게시글 조회
        Board board = boardRepository.findById(boardNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid board ID"));

        // 로그 추가
        System.out.println("Before update - boardFiles: " + board.getBoardFiles());
        System.out.println("Before update - boardLocation: " + board.getBoardLocation());

        // boardFiles와 boardLocation을 명시적으로 설정
        if (boardRequestDTO.getBoardFiles() != null && !boardRequestDTO.getBoardFiles().isEmpty()) {
            board.setBoardFiles(boardRequestDTO.getBoardFiles().trim()); // boardFiles 설정
        }
        if (boardRequestDTO.getBoardLocation() != null && !boardRequestDTO.getBoardLocation().isEmpty()) {
            board.setBoardLocation(boardRequestDTO.getBoardLocation().trim()); // boardLocation 설정
        }

        // 로그 추가
        System.out.println("After update - boardFiles: " + board.getBoardFiles());
        System.out.println("After update - boardLocation: " + board.getBoardLocation());

        // 나머지 필드들은 ModelMapper를 통해 업데이트
        modelMapper.map(boardRequestDTO, board);

        // 변경된 게시글 저장
        board = boardRepository.save(board); // 수정된 엔티티를 DB에 저장

        return modelMapper.map(board, BoardDTO.class); // 수정된 게시글을 DTO로 변환하여 반환
    }

    @Override // 게시글 삭제
    public void deleteBoard(Long boardNo) {
        boardRepository.deleteById(boardNo); // ID로 게시글 삭제
    }
}
