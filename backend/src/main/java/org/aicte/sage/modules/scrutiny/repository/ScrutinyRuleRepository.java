package org.aicte.sage.modules.scrutiny.repository;

import org.aicte.sage.modules.scrutiny.model.ScrutinyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrutinyRuleRepository extends JpaRepository<ScrutinyRule, String> {
    List<ScrutinyRule> findByActiveTrue();
    List<ScrutinyRule> findByApplicationCategoryInAndActiveTrue(List<String> categories);
}
