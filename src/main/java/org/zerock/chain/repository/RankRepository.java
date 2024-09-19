package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Rank;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
    // 추가적인 직급 관련 쿼리 메서드를 정의할 수 있습니다.
}