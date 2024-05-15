package com.ondoset.repository;

import com.ondoset.domain.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Long> {
    List<Metrics> findByModelModelId(Long modelId);
}
