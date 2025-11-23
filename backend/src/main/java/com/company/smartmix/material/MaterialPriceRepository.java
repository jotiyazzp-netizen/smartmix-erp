package com.company.smartmix.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 材料价格Repository
 */
@Repository
public interface MaterialPriceRepository extends JpaRepository<MaterialPrice, Long> {

    Optional<MaterialPrice> findByMaterialAndIsCurrent(Material material, Boolean isCurrent);

    @Modifying
    @Query("UPDATE MaterialPrice mp SET mp.isCurrent = false WHERE mp.material = ?1 AND mp.isCurrent = true")
    void clearCurrentPrices(Material material);
}
