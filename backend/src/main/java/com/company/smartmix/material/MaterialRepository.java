package com.company.smartmix.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 材料Repository
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByMaterialCodeAndPlantCode(String materialCode, String plantCode);

    Optional<Material> findByMaterialCode(String materialCode);
}
