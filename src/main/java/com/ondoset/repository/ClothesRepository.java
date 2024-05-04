package com.ondoset.repository;

import com.ondoset.domain.Clothes;
import com.ondoset.domain.Enum.Category;
import com.ondoset.domain.Enum.Thickness;
import com.ondoset.domain.Member;
import com.ondoset.domain.Tag;
import com.ondoset.dto.clothes.ClothesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothesRepository extends JpaRepository<Clothes, Long> {

	List<Clothes> findByIdIn(List<Long> idList);

	Boolean existsByIdAndMember(Long id, Member member);

	@Query("select ct from Clothes ct where ct.member=:member and ct.isDeleted=false and ct.thickness=:thickness and ct.tag=:tag")
	List<Clothes> findByFullTag(@Param("member") Member member, @Param("thickness") Thickness thickness, @Param("tag") Tag tag);

	@Query("select ct from Clothes ct where ct.member=:member and ct.isDeleted=false and ct.thickness is null and ct.tag=:tag")
	List<Clothes> findByFullTag(@Param("member") Member member, @Param("tag") Tag tag);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member and ct.isDeleted=false order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member and ct.isDeleted=false and ct.id<:lastPage order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member, @Param("lastPage") Long lastPage);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member and t.category=:category and ct.isDeleted=false order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member, @Param("category") Category category);

	@Query("select new com.ondoset.dto.clothes.ClothesDTO(ct.id, ct.name, ct.imageURL, t.category, t.name, ct.thickness)" +
			"from Clothes ct join ct.tag t where ct.member=:member and t.category=:category and ct.isDeleted=false and ct.id<:lastPage " +
			"order by ct.id desc limit 18")
	List<ClothesDTO> pageAllClothes(@Param("member") Member member, @Param("category") Category category, @Param("lastPage") Long lastPage);
}
