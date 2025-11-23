package com.company.smartmix.task;

import com.company.smartmix.common.BusinessException;
import com.company.smartmix.mix.MixRecipe;
import com.company.smartmix.mix.MixRecipeItem;
import com.company.smartmix.mix.MixRecipeRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private final TemplateEngine templateEngine;
    private final ProductionTaskRepository productionTaskRepository;
    private final MixRecipeRepository mixRecipeRepository;

    /**
     * 生成生产任务单PDF
     */
    public byte[] generateTaskPdf(Long taskId) {
        // 查询任务
        ProductionTask task = productionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("生产任务不存在"));

        if (task.getSelectedMixRecipeId() == null) {
            throw new BusinessException("任务尚未选择配比");
        }

        // 查询配比
        MixRecipe recipe = mixRecipeRepository.findByIdWithItems(task.getSelectedMixRecipeId())
                .orElseThrow(() -> new BusinessException("配比不存在"));

        // 准备数据
        TaskPdfData pdfData = preparePdfData(task, recipe);

        // 生成HTML
        Context context = new Context();
        context.setVariable("data", pdfData);
        String html = templateEngine.process("task-pdf", context);

        // 生成PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            log.error("PDF生成失败", e);
            throw new BusinessException("PDF生成失败: " + e.getMessage());
        }
    }

    /**
     * 准备PDF数据
     */
    private TaskPdfData preparePdfData(ProductionTask task, MixRecipe recipe) {
        TaskPdfData data = new TaskPdfData();

        // 任务信息
        data.setTaskNo(task.getTaskNo());
        data.setProjectName(task.getProjectName());
        data.setStrengthGrade(task.getStrengthGrade());
        data.setSlumpRequirement(task.getSlumpRequirement());
        data.setVolume(task.getVolume());
        data.setSpecialRequirements(task.getSpecialRequirements());
        data.setCreatedBy(task.getCreatedBy() != null ? task.getCreatedBy() : "系统");
        data.setCreatedAt(task.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        data.setSourceSystem(task.getSourceSystem().name());

        // 配比信息
        data.setRecipeCode(recipe.getRecipeCode());
        data.setRecipeStrengthGrade(recipe.getStrengthGrade());
        data.setRecipeSlump(recipe.getSlump());

        // 材料清单
        List<MaterialItem> materials = new ArrayList<>();
        for (MixRecipeItem item : recipe.getItems()) {
            MaterialItem material = new MaterialItem();
            material.setMaterialCode(item.getMaterial().getMaterialCode());
            material.setMaterialName(item.getMaterial().getDescription());
            material.setUnit(item.getMaterial().getBaseUnit());
            material.setDosagePerM3(item.getDosagePerM3());
            material.setTotalDosage(item.getDosagePerM3().multiply(task.getVolume()));
            materials.add(material);
        }
        data.setMaterials(materials);

        // 成本信息
        if (task.getTheoreticalUnitCost() != null) {
            data.setUnitCost(task.getTheoreticalUnitCost());
            data.setTotalCost(task.getTheoreticalTotalCost());
        }

        // 生成时间
        data.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return data;
    }

    @Data
    public static class TaskPdfData {
        // 任务信息
        private String taskNo;
        private String projectName;
        private String strengthGrade;
        private String slumpRequirement;
        private BigDecimal volume;
        private String specialRequirements;
        private String createdBy;
        private String createdAt;
        private String sourceSystem;

        // 配比信息
        private String recipeCode;
        private String recipeStrengthGrade;
        private String recipeSlump;

        // 材料清单
        private List<MaterialItem> materials;

        // 成本信息
        private BigDecimal unitCost;
        private BigDecimal totalCost;

        // 生成时间
        private String generatedAt;
    }

    @Data
    public static class MaterialItem {
        private String materialCode;
        private String materialName;
        private String unit;
        private BigDecimal dosagePerM3; // 单方用量
        private BigDecimal totalDosage; // 总用量
    }
}
