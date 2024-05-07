package com.ondoset.repository;

import com.ondoset.common.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {

	List<LogEntity> findTop100ByLevelOrLevelOrderByIdDesc(String error, String warn);
	default List<LogEntity> findTop100ByLevelOrLevelOrderByIdDesc() {
		return findTop100ByLevelOrLevelOrderByIdDesc("ERROR", "WARN");
	}

	Long countByLevelAndMsgAndDateGreaterThan(String level, String msg, Date date);
	default Long countPlan(Date date) {
		return countByLevelAndMsgAndDateGreaterThan("INFO", "plan", date);
	}
	default Long countPast(Date date) {
		return countByLevelAndMsgAndDateGreaterThan("INFO", "past", date);
	}
	default Long countAi(Date date) {
		return countByLevelAndMsgAndDateGreaterThan("INFO", "ai", date);
	}
}
