package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.chain.model.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {
}