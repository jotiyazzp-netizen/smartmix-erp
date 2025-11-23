package com.company.smartmix.mix;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 配比明细Repository
 */
@Repository
public interface MixRecipeItemRepository extends JpaRepository<MixRecipeItem, Long> {
}
