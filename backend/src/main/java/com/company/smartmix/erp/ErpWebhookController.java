package com.company.smartmix.erp;

import com.company.smartmix.common.ApiResponse;
import com.company.smartmix.common.BusinessException;
import com.company.smartmix.material.Material;
import com.company.smartmix.material.MaterialPrice;
import com.company.smartmix.material.MaterialPriceRepository;
import com.company.smartmix.material.MaterialRepository;
import com.company.smartmix.task.ProductionTask;
import com.company.smartmix.task.ProductionTaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ERP数据同步Webhook控制器
 * 接收ERP系统推送的数据
 */
@Slf4j
@Tag(name = "ERP集成", description = "ERP数据同步接口（由ERP主动推送）")
@RestController
@RequestMapping("/api/erp")
@RequiredArgsConstructor
public class ErpWebhookController {

    private final MaterialRepository materialRepository;
    private final MaterialPriceRepository materialPriceRepository;
    private final ProductionTaskRepository productionTaskRepository;
    private final SyncLogRepository syncLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.erp.webhook-token}")
    private String webhookToken;

    /**
     * 同步材料主数据
     */
    @Operation(summary = "同步材料主数据", description = "从ERP接收材料主数据（批量）")
    @SecurityRequirements // 不需要JWT认证
    @PostMapping("/materials")
    @Transactional
    public ApiResponse<SyncResult> syncMaterials(@Valid @RequestBody List<MaterialSyncRequest> requests,
            HttpServletRequest httpRequest) {
        validateWebhookToken(httpRequest);

        SyncLog syncLog = createSyncLog(SyncLog.SyncDirection.ERP_TO_SMARTMIX,
                SyncLog.DataType.MATERIAL,
                requests,
                httpRequest.getRemoteAddr());

        try {
            int successCount = 0;
            int failureCount = 0;

            for (MaterialSyncRequest request : requests) {
                try {
                    String plantCode = request.getPlantCode() != null ? request.getPlantCode() : "DEFAULT";
                    Material material = materialRepository
                            .findByMaterialCodeAndPlantCode(request.getMaterialCode(), plantCode)
                            .orElse(new Material());

                    material.setMaterialCode(request.getMaterialCode());
                    material.setDescription(request.getDescription());
                    material.setSpec(request.getSpec());
                    material.setBaseUnit(request.getBaseUnit());
                    material.setPlantCode(plantCode);
                    material.setSourceSystem("SAP-MM");

                    materialRepository.save(material);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to sync material: {}", request.getMaterialCode(), e);
                    failureCount++;
                }
            }

            syncLog.setStatus(SyncLog.SyncStatus.SUCCESS);
            syncLogRepository.save(syncLog);

            return ApiResponse.success(new SyncResult(successCount, failureCount));
        } catch (Exception e) {
            syncLog.setStatus(SyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLogRepository.save(syncLog);
            throw new BusinessException("材料同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步材料价格
     */
    @Operation(summary = "同步材料价格", description = "从ERP接收材料价格（批量）")
    @SecurityRequirements
    @PostMapping("/material-prices")
    @Transactional
    public ApiResponse<SyncResult> syncMaterialPrices(@Valid @RequestBody List<MaterialPriceSyncRequest> requests,
            HttpServletRequest httpRequest) {
        validateWebhookToken(httpRequest);

        SyncLog syncLog = createSyncLog(SyncLog.SyncDirection.ERP_TO_SMARTMIX,
                SyncLog.DataType.MATERIAL_PRICE,
                requests,
                httpRequest.getRemoteAddr());

        try {
            int successCount = 0;
            int failureCount = 0;

            for (MaterialPriceSyncRequest request : requests) {
                try {
                    String plantCode = request.getPlantCode() != null ? request.getPlantCode() : "DEFAULT";
                    Material material = materialRepository
                            .findByMaterialCodeAndPlantCode(request.getMaterialCode(), plantCode)
                            .orElseThrow(() -> new BusinessException(
                                    "材料不存在: " + request.getMaterialCode() + " (Plant: " + plantCode + ")"));

                    // 清除该材料的当前价格标记
                    materialPriceRepository.clearCurrentPrices(material);

                    // 创建新价格记录
                    MaterialPrice price = new MaterialPrice();
                    price.setMaterial(material);
                    price.setPrice(request.getPrice());
                    price.setPriceUnit(request.getPriceUnit());
                    price.setCurrency(request.getCurrency());
                    price.setEffectiveFrom(parseDateTime(request.getEffectiveFrom()));
                    price.setIsCurrent(true);
                    price.setSourceSystem(request.getSourceSystem());

                    // 计算 pricePerKg（元/吨 → 元/公斤）
                    if ("YuanPerTon".equalsIgnoreCase(request.getPriceUnit())) {
                        price.setPricePerKg(request.getPrice().divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP));
                    } else {
                        price.setPricePerKg(request.getPrice());
                    }

                    materialPriceRepository.save(price);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to sync material price: {}", request.getMaterialCode(), e);
                    failureCount++;
                }
            }

            syncLog.setStatus(SyncLog.SyncStatus.SUCCESS);
            syncLogRepository.save(syncLog);

            return ApiResponse.success(new SyncResult(successCount, failureCount));
        } catch (Exception e) {
            syncLog.setStatus(SyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLogRepository.save(syncLog);
            throw new BusinessException("价格同步失败: " + e.getMessage());
        }
    }

    /**
     * 同步生产任务
     */
    @Operation(summary = "同步生产任务", description = "从ERP接收生产任务")
    @SecurityRequirements
    @PostMapping("/production-tasks")
    @Transactional
    public ApiResponse<SyncResult> syncProductionTasks(@Valid @RequestBody List<ProductionTaskSyncRequest> requests,
            HttpServletRequest httpRequest) {
        validateWebhookToken(httpRequest);

        SyncLog syncLog = createSyncLog(SyncLog.SyncDirection.ERP_TO_SMARTMIX,
                SyncLog.DataType.PRODUCTION_TASK,
                requests,
                httpRequest.getRemoteAddr());

        try {
            int successCount = 0;
            int failureCount = 0;

            for (ProductionTaskSyncRequest request : requests) {
                try {
                    ProductionTask task = productionTaskRepository
                            .findByTaskNo(request.getTaskNo())
                            .orElse(new ProductionTask());

                    task.setTaskNo(request.getTaskNo());
                    task.setProjectName(request.getProjectName());
                    task.setStrengthGrade(request.getStrengthGrade());
                    task.setSlumpRequirement(request.getSlumpRequirement());
                    task.setVolume(request.getVolume());
                    task.setSpecialRequirements(request.getSpecialRequirements());
                    task.setSourceSystem(ProductionTask.SourceSystem.SAP);
                    task.setSapSalesOrderNo(request.getSapSalesOrderNo());
                    task.setSapProductionOrderNo(request.getSapProductionOrderNo());

                    if (task.getStatus() == null) {
                        task.setStatus(ProductionTask.TaskStatus.NEW);
                    }

                    productionTaskRepository.save(task);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to sync production task: {}", request.getTaskNo(), e);
                    failureCount++;
                }
            }

            syncLog.setStatus(SyncLog.SyncStatus.SUCCESS);
            syncLogRepository.save(syncLog);

            return ApiResponse.success(new SyncResult(successCount, failureCount));
        } catch (Exception e) {
            syncLog.setStatus(SyncLog.SyncStatus.FAILED);
            syncLog.setErrorMessage(e.getMessage());
            syncLogRepository.save(syncLog);
            throw new BusinessException("生产任务同步失败: " + e.getMessage());
        }
    }

    /**
     * 验证Webhook Token
     */
    private void validateWebhookToken(HttpServletRequest request) {
        String token = request.getHeader("X-ERP-TOKEN");
        if (token == null || !token.equals(webhookToken)) {
            throw new BusinessException(403, "Invalid webhook token");
        }
    }

    /**
     * 创建同步日志
     */
    private SyncLog createSyncLog(SyncLog.SyncDirection direction,
            SyncLog.DataType dataType,
            Object payload,
            String sourceIp) {
        SyncLog log = new SyncLog();
        log.setDirection(direction);
        log.setDataType(dataType);
        log.setSourceIp(sourceIp);
        try {
            log.setPayload(objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            log.setPayload(payload.toString());
        }
        return log;
    }

    /**
     * 解析日期时间
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    // DTO类

    @Data
    public static class MaterialSyncRequest {
        @NotBlank(message = "材料编码不能为空")
        private String materialCode;

        @NotBlank(message = "材料描述不能为空")
        private String description;

        private String spec;

        @NotBlank(message = "基本单位不能为空")
        private String baseUnit;

        private String plantCode;
    }

    @Data
    public static class MaterialPriceSyncRequest {
        @NotBlank(message = "材料编码不能为空")
        private String materialCode;

        private String plantCode;

        @NotNull(message = "价格不能为空")
        private BigDecimal price;

        private String priceUnit = "YuanPerTon";
        private String currency = "CNY";

        @NotBlank(message = "生效日期不能为空")
        private String effectiveFrom;

        private String sourceSystem = "SAP-MM";
    }

    @Data
    public static class ProductionTaskSyncRequest {
        @NotBlank(message = "任务单号不能为空")
        private String taskNo;

        @NotBlank(message = "工程名称不能为空")
        private String projectName;

        @NotBlank(message = "强度等级不能为空")
        private String strengthGrade;

        private String slumpRequirement;

        @NotNull(message = "方量不能为空")
        private BigDecimal volume;

        private String specialRequirements;
        private String sapSalesOrderNo;
        private String sapProductionOrderNo;
    }

    @Data
    public static class SyncResult {
        private int successCount;
        private int failureCount;

        public SyncResult(int successCount, int failureCount) {
            this.successCount = successCount;
            this.failureCount = failureCount;
        }
    }
}
