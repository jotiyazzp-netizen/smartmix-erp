package com.company.smartmix.mix;

import com.company.smartmix.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 配比方案实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mix_recipes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "recipeCode")
})
public class MixRecipe extends BaseEntity {

    /**
     * 配比编号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String recipeCode;

    /**
     * 强度等级（如 C30）
     */
    @Column(nullable = false, length = 20)
    private String strengthGrade;

    /**
     * 坍落度
     */
    @Column(length = 50)
    private String slump;

    /**
     * 特殊技术要求
     */
    @Column(columnDefinition = "TEXT")
    private String technicalRequirements;

    /**
     * 配比状态
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RecipeStatus status = RecipeStatus.PENDING_APPROVAL;

    /**
     * 配比材料清单
     */
    @OneToMany(mappedBy = "mixRecipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MixRecipeItem> items = new ArrayList<>();

    /**
     * 备注
     */
    @Column(columnDefinition = "TEXT")
    private String remarks;

    public enum RecipeStatus {
        PENDING_APPROVAL, // 待审核
        APPROVED, // 已审核
        DISABLED // 已停用
    }

    /**
     * 添加配比明细
     */
    public void addItem(MixRecipeItem item) {
        items.add(item);
        item.setMixRecipe(this);
    }

    /**
     * 移除配比明细
     */
    public void removeItem(MixRecipeItem item) {
        items.remove(item);
        item.setMixRecipe(null);
    }
}
