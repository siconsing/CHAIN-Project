package org.zerock.chain.service;

import org.springframework.stereotype.Service;
import org.zerock.chain.dto.FormDTO;

@Service
public interface FormService {
    FormDTO getFormByCategory(String category); // 특정 category로 저장된 form_html을 조회하는 메서드
}
