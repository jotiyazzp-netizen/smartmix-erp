package com.company.smartmix.mix;

import com.company.smartmix.common.BaseEntity;
import com.company.smartmix.material.Material;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配比明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true, exclude = "mixRecipe")
@Entity
@Table(name = "mix_recipe_items")
public class MixRecipeItem extends BaseEntity {

    /**
     * 所属配比
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mix_recipe_id", nullable = false)
    private MixRecipe mixRecipe;

    /**
     * 材料
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * 单方用量（kg/m³）
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dosagePerM3;

    /**
     * 备注
     */
    @Column(length = 200)
    private String remarks;
}
