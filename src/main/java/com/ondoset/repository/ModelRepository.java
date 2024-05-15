package com.ondoset.repository;

import com.ondoset.domain.Member;
import com.ondoset.domain.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    // adapt가 true인 모델을 찾는다.
    Model findByAdaptTrue();

    // 모든 모델 조회
    List<Model> findAll();

    // 가장 최근 훈련된 모델 조회
    Model findFirstByOrderByModelIdDesc();

    Model findFirstByOrderByModelVersionDesc();
}
