package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
}
