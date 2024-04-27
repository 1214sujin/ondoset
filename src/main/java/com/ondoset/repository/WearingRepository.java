package com.ondoset.repository;

import com.ondoset.domain.OOTD;
import com.ondoset.domain.Wearing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WearingRepository extends JpaRepository<Wearing, Long> {

	List<Wearing> findByOotd(OOTD ootd);
}
