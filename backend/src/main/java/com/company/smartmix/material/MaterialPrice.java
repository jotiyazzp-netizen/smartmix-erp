package com.company.smartmix.material;

import com.company.smartmix.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 材料价格实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "material_prices", indexes = {
        @Index(name = "idx_material_effective", columnList = "material_id, isCurrent"),
        @Index(name = "idx_effective_from", columnList = "effectiveFrom")
})
public class MaterialPrice extends BaseEntity {

    /**
     * 关联材料
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    /**
     * 价格（单位：元/吨）
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 价格单位（YuanPerTon等）
     */
    @Column(length = 20)
    private String priceUnit = "YuanPerTon";

    /**
     * 币种
     */
    @Column(length = 10)
    private String currency = "CNY";

    /**
     * 生效日期
     */
    @Column(nullable = false)
    private LocalDateTime effectiveFrom;

    /**
     * 是否当前生效价格
     */
    @Column(nullable = false)
    private Boolean isCurrent = false;

    /**
     * 折算后的价格（元/公斤）
     */
    @Column(precision = 10, scale = 4)
    private BigDecimal pricePerKg;

    /**
     * 来源系统
     */
    @Column(length = 50)
    private String sourceSystem = "SAP-MM";
}
