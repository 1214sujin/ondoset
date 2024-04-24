package com.ondoset.repository;

import com.ondoset.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	@Query("select new com.ondoset.dto.clothes.Tag(t.name, t.id) from Tag t where t.category='TOP'")
	List<com.ondoset.dto.clothes.Tag> findTop();

	@Query("select new com.ondoset.dto.clothes.Tag(t.name, t.id) from Tag t where t.category='BOTTOM'")
	List<com.ondoset.dto.clothes.Tag> findBottom();

	@Query("select new com.ondoset.dto.clothes.Tag(t.name, t.id) from Tag t where t.category='OUTER'")
	List<com.ondoset.dto.clothes.Tag> findOuter();

	@Query("select new com.ondoset.dto.clothes.Tag(t.name, t.id) from Tag t where t.category='SHOE'")
	List<com.ondoset.dto.clothes.Tag> findShoe();

	@Query("select new com.ondoset.dto.clothes.Tag(t.name, t.id) from Tag t where t.category='ACC'")
	List<com.ondoset.dto.clothes.Tag> findAcc();
}
