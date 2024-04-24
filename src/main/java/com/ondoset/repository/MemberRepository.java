package com.ondoset.repository;

import com.ondoset.domain.Member;
import com.ondoset.dto.admin.blacklist.GetRootDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

	Boolean existsByName(String name);

	Boolean existsByNickname(String nickname);

	Member findByName(String name);

	@Query("select new com.ondoset.dto.admin.blacklist.GetRootDTO(m.id, m.nickname, m.banPeriod) " +
			"from Member m where m.banPeriod>0")
	List<GetRootDTO> findByBanPeriodGreaterThan();
}
