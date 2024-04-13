package com.ondoset.repository;

import com.ondoset.domain.OnBoarding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OnBoardingRepository extends JpaRepository<OnBoarding, Long> {
}
