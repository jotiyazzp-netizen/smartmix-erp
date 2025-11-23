package com.company.smartmix.material;

import com.company.smartmix.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 材料主数据实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "materials", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "materialCode", "plantCode" })
})
public class Material extends BaseEntity {

    /**
     * 材料编码（SAP MATNR）
     */
    @Column(nullable = false, length = 40)
    private String materialCode;

    /**
     * 材料描述/名称
     */
    @Column(nullable = false, length = 200)
    private String description;

    /**
     * 规格
     */
    @Column(length = 200)
    private String spec;

    /**
     * 基本计量单位（KG/T等）
     */
    @Column(nullable = false, length = 10)
    private String baseUnit;

    /**
     * 工厂编码（SAP WERKS）
     */
    @Column(length = 10)
    private String plantCode;

    /**
     * 来源系统
     */
    @Column(length = 50)
    private String sourceSystem = "SAP-MM";
}
