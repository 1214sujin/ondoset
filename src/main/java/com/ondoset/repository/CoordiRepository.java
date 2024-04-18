package com.ondoset.repository;

import com.ondoset.domain.Coordi;
import com.ondoset.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordiRepository extends JpaRepository<Coordi, Long> {

	Coordi findByConsistings_Clothes_MemberAndDate(Member member, Long date);
}
