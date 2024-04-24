package com.ondoset.repository;

import com.ondoset.domain.Clothes;
import com.ondoset.domain.Member;
import com.ondoset.dto.clothes.ClothesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long> {

	List<Clothes> findByIdIn(List<Long> idList);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member and ct.id<:lastPage order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member, @Param("lastPage") Long lastPage);
}
