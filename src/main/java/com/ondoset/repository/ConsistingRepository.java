package com.ondoset.repository;

import com.ondoset.domain.Consisting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsistingRepository extends JpaRepository<Consisting, Long> {
}
