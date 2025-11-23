package com.company.smartmix.task;

import com.company.smartmix.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 生产任务实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "production_tasks", uniqueConstraints = {
        @UniqueConstraint(columnNames = "taskNo")
})
public class ProductionTask extends BaseEntity {

    /**
     * 任务单号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String taskNo;

    /**
     * 工程名称
     */
    @Column(nullable = false, length = 200)
    private String projectName;

    /**
     * 强度等级（如 C30）
     */
    @Column(nullable = false, length = 20)
    private String strengthGrade;

    /**
     * 坍落度要求
     */
    @Column(length = 50)
    private String slumpRequirement;

    /**
     * 需求方量（m³）
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal volume;

    /**
     * 特殊技术要求
     */
    @Column(columnDefinition = "TEXT")
    private String specialRequirements;

    /**
     * 来源系统（SAP / MANUAL）
     */
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private SourceSystem sourceSystem = SourceSystem.MANUAL;

    /**
     * SAP 销售订单号
     */
    @Column(length = 50)
    private String sapSalesOrderNo;

    /**
     * SAP 生产订单号
     */
    @Column(length = 50)
    private String sapProductionOrderNo;

    /**
     * 任务状态
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.NEW;

    /**
     * 选定的配比ID
     */
    @Column
    private Long selectedMixRecipeId;

    /**
     * 理论单方成本（元/m³）
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal theoreticalUnitCost;

    /**
     * 理论总成本（元）
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal theoreticalTotalCost;

    public enum SourceSystem {
        SAP,
        MANUAL
    }

    public enum TaskStatus {
        NEW, // 新建
        PLANNED, // 已计划（已选配比）
        IN_PROGRESS, // 生产中
        COMPLETED, // 已完成
        CANCELLED // 已取消
    }
}
