package org.zerock.chain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zerock.chain.dto.FormDTO;
import org.zerock.chain.model.Form;
import org.zerock.chain.repository.FormRepository;

@Service
public class FormServiceImpl implements FormService {

    @Autowired
    private FormRepository formRepository;

    @Override
    public FormDTO getFormByCategory(String category) {
        // category에 해당하는 form을 DB에서 찾아서 반환
        Form form = formRepository.findById(category).orElse(null);
        if (form != null) {
            // Form 엔티티를 FormDTO로 변환하여 반환
            return FormDTO.builder()
                    .category(form.getCategory())
                    .formHtml(form.getFormHtml())
                    .build();
        } else {
            // 필요에 따라 null 또는 예외 처리
            return null;
        }
    }
}
