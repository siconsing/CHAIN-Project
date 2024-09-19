package org.zerock.chain.service;


import org.zerock.chain.dto.FavoriteQnaDTO;
import org.zerock.chain.dto.FavoriteQnaRequestDTO;

import java.util.List;

public interface FavoriteQnaService {
    List<FavoriteQnaDTO> getAllFAQs();
    FavoriteQnaDTO getFAQById(Long faqNo);
    FavoriteQnaDTO createFAQ(FavoriteQnaRequestDTO favoriteQnaRequestDTO);
    // FAQ 수정 메소드
    void updateFaq(Long faqNo, String faqName, String faqContent);
    // FAQ 삭제 메소드
    void deleteFaq(Long faqNo);
}
