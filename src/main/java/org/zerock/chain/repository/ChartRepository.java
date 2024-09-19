package org.zerock.chain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zerock.chain.model.Chart;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartRepository extends JpaRepository<Chart, Long> {

    // 특정 작성자(chartAuthor)에 해당하는 모든 Chart 항목을 찾는 메서드
    List<Chart> findByChartAuthor(Long chartAuthor);
    // 특정 차트 번호(chartNo)와 작성자(chartAuthor)에 해당하는 Chart를 찾는 메서드
    Optional<Chart> findByChartNoAndChartAuthor(Long chartNo, Long chartAuthor);
}
