package com.company.smartmix.erp;

import com.company.smartmix.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 同步日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sync_logs", indexes = {
        @Index(name = "idx_direction_type_created", columnList = "direction,dataType,createdAt")
})
public class SyncLog extends BaseEntity {

    /**
     * 同步方向（ERP_TO_SMARTMIX / SMARTMIX_TO_ERP）
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncDirection direction;

    /**
     * 数据类型（MATERIAL / MATERIAL_PRICE / PRODUCTION_TASK等）
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private DataType dataType;

    /**
     * 请求Payload（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * 同步状态（SUCCESS / FAILED）
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SyncStatus status;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 来源IP
     */
    @Column(length = 50)
    private String sourceIp;

    public enum SyncDirection {
        ERP_TO_SMARTMIX,
        SMARTMIX_TO_ERP
    }

    public enum DataType {
        MATERIAL,
        MATERIAL_PRICE,
        PRODUCTION_TASK,
        THEORETICAL_CONSUMPTION,
        COST_DATA
    }

    public enum SyncStatus {
        SUCCESS,
        FAILED
    }
}
