package org.zerock.chain.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.FavoriteQnaDTO;
import org.zerock.chain.dto.FavoriteQnaRequestDTO;
import org.zerock.chain.model.FavoriteQna;
import org.zerock.chain.repository.FavoriteQnaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteQnaServiceImpl implements FavoriteQnaService {

    private final FavoriteQnaRepository favoriteQnaRepository;

    @Autowired
    public FavoriteQnaServiceImpl(FavoriteQnaRepository favoriteQnaRepository) {
        this.favoriteQnaRepository = favoriteQnaRepository;
    }

    @Override
    public List<FavoriteQnaDTO> getAllFAQs() {
        return favoriteQnaRepository.findAll().stream()
                .map(faq -> new FavoriteQnaDTO(
                        faq.getFaqNo(),
                        faq.getFaqName(),
                        faq.getFaqContent(),
                        faq.getFaqCreatedDate()  // 추가된 필드 반영
                ))
                .collect(Collectors.toList());
    }

    @Override
    public FavoriteQnaDTO getFAQById(Long faqNo) {
        FavoriteQna faq = favoriteQnaRepository.findById(faqNo)
                .orElseThrow(() -> new IllegalArgumentException("Invalid FAQ ID: " + faqNo));
        return new FavoriteQnaDTO(
                faq.getFaqNo(),
                faq.getFaqName(),
                faq.getFaqContent(),
                faq.getFaqCreatedDate()  // 추가된 필드 반영
        );
    }

    @Override
    public FavoriteQnaDTO createFAQ(FavoriteQnaRequestDTO favoriteQnaRequestDTO) {
        FavoriteQna faq = new FavoriteQna(
                favoriteQnaRequestDTO.getFaqName(),
                favoriteQnaRequestDTO.getFaqContent()
        );
        FavoriteQna savedFaq = favoriteQnaRepository.save(faq);
        return new FavoriteQnaDTO(
                savedFaq.getFaqNo(),
                savedFaq.getFaqName(),
                savedFaq.getFaqContent(),
                savedFaq.getFaqCreatedDate()  // 추가된 필드 반영
        );
    }

    @Override
    public void updateFaq(Long faqNo, String faqName, String faqContent) {
        FavoriteQna faq = favoriteQnaRepository.findById(faqNo)
                .orElseThrow(() -> new EntityNotFoundException("FAQ not found with id " + faqNo));

        faq.setFaqName(faqName);
        faq.setFaqContent(faqContent);

        favoriteQnaRepository.save(faq);
    }

    @Override
    public void deleteFaq(Long faqNo) {
        favoriteQnaRepository.deleteById(faqNo);
    }

}
