package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.EQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EQuestionsRepository extends JpaRepository<EQuestions, Long> {

   List<EQuestions> findAllByLevelKey(String level);

}