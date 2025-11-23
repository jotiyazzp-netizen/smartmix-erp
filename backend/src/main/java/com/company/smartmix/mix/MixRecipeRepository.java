package com.company.smartmix.mix;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 配比Repository
 */
@Repository
public interface MixRecipeRepository extends JpaRepository<MixRecipe, Long> {

    Optional<MixRecipe> findByRecipeCode(String recipeCode);

    boolean existsByRecipeCode(String recipeCode);

    Page<MixRecipe> findByStrengthGradeAndStatus(String strengthGrade, MixRecipe.RecipeStatus status,
            Pageable pageable);

    Page<MixRecipe> findByStatus(MixRecipe.RecipeStatus status, Pageable pageable);

    @Query("SELECT r FROM MixRecipe r LEFT JOIN FETCH r.items WHERE r.id = ?1")
    Optional<MixRecipe> findByIdWithItems(Long id);

    List<MixRecipe> findByStrengthGradeAndStatus(String strengthGrade, MixRecipe.RecipeStatus status);
}
