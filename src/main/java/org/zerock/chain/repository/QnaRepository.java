package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.chain.model.Qna;

public interface QnaRepository extends JpaRepository<Qna, Long> {
}