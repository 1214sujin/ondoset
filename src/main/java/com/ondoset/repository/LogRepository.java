package com.ondoset.repository;

import com.ondoset.common.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {

	List<LogEntity> findTop100ByLevelOrLevelOrderByIdDesc(String error, String warn);
	default List<LogEntity> findTop100ByLevelOrLevelOrderByIdDesc() {
		return findTop100ByLevelOrLevelOrderByIdDesc("ERROR", "WARN");
	}
}
