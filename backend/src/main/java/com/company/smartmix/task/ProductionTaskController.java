package com.company.smartmix.task;

import com.company.smartmix.common.ApiResponse;
import com.company.smartmix.common.BusinessException;
import com.company.smartmix.cost.CostOptimizationController;
import com.company.smartmix.mix.MixRecipe;
import com.company.smartmix.mix.MixRecipeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产任务控制器
 */
@Tag(name = "生产任务", description = "生产任务管理")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class ProductionTaskController {

    private final ProductionTaskRepository productionTaskRepository;
    private final MixRecipeRepository mixRecipeRepository;
    private final PdfGenerationService pdfGenerationService;

    @Operation(summary = "分页查询生产任务")
    @GetMapping
    public ApiResponse<Page<ProductionTaskListDTO>> getAllTasks(
            @RequestParam(required = false) ProductionTask.TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductionTask> tasks = productionTaskRepository.findAll(pageable);

        Page<ProductionTaskListDTO> result = tasks.map(this::toListDTO);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询任务详情")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ApiResponse<ProductionTaskDetailDTO> getTaskById(@PathVariable Long id) {
        ProductionTask task = productionTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产任务不存在"));

        return ApiResponse.success(toDetailDTO(task));
    }

    @Operation(summary = "创建生产任务")
    @PostMapping
    @Transactional
    public ApiResponse<ProductionTaskDetailDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        if (productionTaskRepository.existsByTaskNo(request.getTaskNo())) {
            return ApiResponse.badRequest("任务单号已存在");
        }

        ProductionTask task = new ProductionTask();
        task.setTaskNo(request.getTaskNo());
        task.setProjectName(request.getProjectName());
        task.setStrengthGrade(request.getStrengthGrade());
        task.setSlumpRequirement(request.getSlumpRequirement());
        task.setVolume(request.getVolume());
        task.setSpecialRequirements(request.getSpecialRequirements());
        task.setSourceSystem(ProductionTask.SourceSystem.MANUAL);
        task.setStatus(ProductionTask.TaskStatus.NEW);

        ProductionTask saved = productionTaskRepository.save(task);
        return ApiResponse.success("任务创建成功", toDetailDTO(saved));
    }

    @Operation(summary = "选择配比")
    @PostMapping("/{id}/select-mix")
    @Transactional
    public ApiResponse<ProductionTaskDetailDTO> selectMix(@PathVariable Long id,
            @Valid @RequestBody SelectMixRequest request) {
        ProductionTask task = productionTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException("生产任务不存在"));

        MixRecipe recipe = mixRecipeRepository.findByIdWithItems(request.getMixRecipeId())
                .orElseThrow(() -> new BusinessException("配比不存在"));

        if (recipe.getStatus() != MixRecipe.RecipeStatus.APPROVED) {
            return ApiResponse.badRequest("只能选择已审核的配比");
        }

        // 更新任务
        task.setSelectedMixRecipeId(recipe.getId());

        // 设置理论成本
        if (request.getTheoreticalUnitCost() != null) {
            task.setTheoreticalUnitCost(request.getTheoreticalUnitCost());
            task.setTheoreticalTotalCost(request.getTheoreticalUnitCost().multiply(task.getVolume()));
        }

        task.setStatus(ProductionTask.TaskStatus.PLANNED);

        ProductionTask saved = productionTaskRepository.save(task);
        return ApiResponse.success("配比选择成功", toDetailDTO(saved));
    }

    @Operation(summary = "生成PDF任务单")
    @GetMapping("/{id}/pdf")
    public org.springframework.http.ResponseEntity<byte[]> generatePdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfGenerationService.generateTaskPdf(id);

        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=task-" + id + ".pdf")
                .body(pdfBytes);
    }

    // DTO 转换方法

    private ProductionTaskListDTO toListDTO(ProductionTask task) {
        ProductionTaskListDTO dto = new ProductionTaskListDTO();
        dto.setId(task.getId());
        dto.setTaskNo(task.getTaskNo());
        dto.setProjectName(task.getProjectName());
        dto.setStrengthGrade(task.getStrengthGrade());
        dto.setVolume(task.getVolume());
        dto.setStatus(task.getStatus());
        dto.setSourceSystem(task.getSourceSystem());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    private ProductionTaskDetailDTO toDetailDTO(ProductionTask task) {
        ProductionTaskDetailDTO dto = new ProductionTaskDetailDTO();
        dto.setId(task.getId());
        dto.setTaskNo(task.getTaskNo());
        dto.setProjectName(task.getProjectName());
        dto.setStrengthGrade(task.getStrengthGrade());
        dto.setSlumpRequirement(task.getSlumpRequirement());
        dto.setVolume(task.getVolume());
        dto.setSpecialRequirements(task.getSpecialRequirements());
        dto.setStatus(task.getStatus());
        dto.setSourceSystem(task.getSourceSystem());
        dto.setSapSalesOrderNo(task.getSapSalesOrderNo());
        dto.setSapProductionOrderNo(task.getSapProductionOrderNo());
        dto.setSelectedMixRecipeId(task.getSelectedMixRecipeId());
        dto.setTheoreticalUnitCost(task.getTheoreticalUnitCost());
        dto.setTheoreticalTotalCost(task.getTheoreticalTotalCost());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setCreatedBy(task.getCreatedBy());
        return dto;
    }

    // DTOs

    @Data
    public static class ProductionTaskListDTO {
        private Long id;
        private String taskNo;
        private String projectName;
        private String strengthGrade;
        private BigDecimal volume;
        private ProductionTask.TaskStatus status;
        private ProductionTask.SourceSystem sourceSystem;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ProductionTaskDetailDTO {
        private Long id;
        private String taskNo;
        private String projectName;
        private String strengthGrade;
        private String slumpRequirement;
        private BigDecimal volume;
        private String specialRequirements;
        private ProductionTask.TaskStatus status;
        private ProductionTask.SourceSystem sourceSystem;
        private String sapSalesOrderNo;
        private String sapProductionOrderNo;
        private Long selectedMixRecipeId;
        private BigDecimal theoreticalUnitCost;
        private BigDecimal theoreticalTotalCost;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    @Data
    public static class CreateTaskRequest {
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
    }

    @Data
    public static class SelectMixRequest {
        @NotNull(message = "配比ID不能为空")
        private Long mixRecipeId;

        private BigDecimal theoreticalUnitCost;
    }
}
