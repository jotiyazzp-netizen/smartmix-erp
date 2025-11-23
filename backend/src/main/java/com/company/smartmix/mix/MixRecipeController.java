package com.company.smartmix.mix;

import com.company.smartmix.common.ApiResponse;
import com.company.smartmix.common.BusinessException;
import com.company.smartmix.material.Material;
import com.company.smartmix.material.MaterialRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 配比管理控制器
 */
@Tag(name = "配比管理", description = "混凝土配比方案管理")
@RestController
@RequestMapping("/api/mix/recipes")
@RequiredArgsConstructor
public class MixRecipeController {

    private final MixRecipeRepository mixRecipeRepository;
    private final MixRecipeItemRepository mixRecipeItemRepository;
    private final MaterialRepository materialRepository;

    @Operation(summary = "分页查询配比")
    @GetMapping
    public ApiResponse<Page<MixRecipeListDTO>> getAllRecipes(
            @RequestParam(required = false) String strengthGrade,
            @RequestParam(required = false) MixRecipe.RecipeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MixRecipe> recipes;

        if (strengthGrade != null && status != null) {
            recipes = mixRecipeRepository.findByStrengthGradeAndStatus(strengthGrade, status, pageable);
        } else if (status != null) {
            recipes = mixRecipeRepository.findByStatus(status, pageable);
        } else {
            recipes = mixRecipeRepository.findAll(pageable);
        }

        Page<MixRecipeListDTO> result = recipes.map(this::toListDTO);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询配比详情")
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ApiResponse<MixRecipeDetailDTO> getRecipeById(@PathVariable Long id) {
        MixRecipe recipe = mixRecipeRepository.findByIdWithItems(id)
                .orElseThrow(() -> new BusinessException("配比不存在"));

        return ApiResponse.success(toDetailDTO(recipe));
    }

    @Operation(summary = "创建配比")
    @PostMapping
    @Transactional
    public ApiResponse<MixRecipeDetailDTO> createRecipe(@Valid @RequestBody CreateMixRecipeRequest request) {
        if (mixRecipeRepository.existsByRecipeCode(request.getRecipeCode())) {
            return ApiResponse.badRequest("配比编号已存在");
        }

        MixRecipe recipe = new MixRecipe();
        recipe.setRecipeCode(request.getRecipeCode());
        recipe.setStrengthGrade(request.getStrengthGrade());
        recipe.setSlump(request.getSlump());
        recipe.setTechnicalRequirements(request.getTechnicalRequirements());
        recipe.setRemarks(request.getRemarks());
        recipe.setStatus(MixRecipe.RecipeStatus.PENDING_APPROVAL);

        // 添加材料清单
        for (MixRecipeItemRequest itemReq : request.getItems()) {
            Material material = materialRepository.findById(itemReq.getMaterialId())
                    .orElseThrow(() -> new BusinessException("材料不存在: " + itemReq.getMaterialId()));

            MixRecipeItem item = new MixRecipeItem();
            item.setMaterial(material);
            item.setDosagePerM3(itemReq.getDosagePerM3());
            item.setRemarks(itemReq.getRemarks());

            recipe.addItem(item);
        }

        MixRecipe saved = mixRecipeRepository.save(recipe);
        return ApiResponse.success("配比创建成功", toDetailDTO(saved));
    }

    @Operation(summary = "编辑配比")
    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<MixRecipeDetailDTO> updateRecipe(@PathVariable Long id,
            @Valid @RequestBody UpdateMixRecipeRequest request) {
        MixRecipe recipe = mixRecipeRepository.findByIdWithItems(id)
                .orElseThrow(() -> new BusinessException("配比不存在"));

        if (recipe.getStatus() != MixRecipe.RecipeStatus.PENDING_APPROVAL) {
            return ApiResponse.badRequest("只能编辑待审核状态的配比");
        }

        recipe.setSlump(request.getSlump());
        recipe.setTechnicalRequirements(request.getTechnicalRequirements());
        recipe.setRemarks(request.getRemarks());

        // 更新材料清单
        recipe.getItems().clear();
        for (MixRecipeItemRequest itemReq : request.getItems()) {
            Material material = materialRepository.findById(itemReq.getMaterialId())
                    .orElseThrow(() -> new BusinessException("材料不存在: " + itemReq.getMaterialId()));

            MixRecipeItem item = new MixRecipeItem();
            item.setMaterial(material);
            item.setDosagePerM3(itemReq.getDosagePerM3());
            item.setRemarks(itemReq.getRemarks());

            recipe.addItem(item);
        }

        MixRecipe saved = mixRecipeRepository.save(recipe);
        return ApiResponse.success("配比更新成功", toDetailDTO(saved));
    }

    @Operation(summary = "审核通过配比")
    @PostMapping("/{id}/approve")
    @Transactional
    public ApiResponse<String> approveRecipe(@PathVariable Long id) {
        MixRecipe recipe = mixRecipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配比不存在"));

        if (recipe.getStatus() != MixRecipe.RecipeStatus.PENDING_APPROVAL) {
            return ApiResponse.badRequest("只能审核待审核状态的配比");
        }

        recipe.setStatus(MixRecipe.RecipeStatus.APPROVED);
        mixRecipeRepository.save(recipe);

        return ApiResponse.success("配比审核成功");
    }

    @Operation(summary = "停用配比")
    @PostMapping("/{id}/disable")
    @Transactional
    public ApiResponse<String> disableRecipe(@PathVariable Long id) {
        MixRecipe recipe = mixRecipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("配比不存在"));

        recipe.setStatus(MixRecipe.RecipeStatus.DISABLED);
        mixRecipeRepository.save(recipe);

        return ApiResponse.success("配比已停用");
    }

    @Operation(summary = "复制配比")
    @PostMapping("/{id}/copy")
    @Transactional
    public ApiResponse<MixRecipeDetailDTO> copyRecipe(@PathVariable Long id,
            @RequestBody(required = false) CopyRecipeRequest request) {
        MixRecipe source = mixRecipeRepository.findByIdWithItems(id)
                .orElseThrow(() -> new BusinessException("源配比不存在"));

        // 生成新配比编号
        String newCode = request != null && request.getNewRecipeCode() != null
                ? request.getNewRecipeCode()
                : source.getRecipeCode() + "-COPY-"
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        if (mixRecipeRepository.existsByRecipeCode(newCode)) {
            return ApiResponse.badRequest("配比编号已存在");
        }

        MixRecipe newRecipe = new MixRecipe();
        newRecipe.setRecipeCode(newCode);
        newRecipe.setStrengthGrade(source.getStrengthGrade());
        newRecipe.setSlump(source.getSlump());
        newRecipe.setTechnicalRequirements(source.getTechnicalRequirements());
        newRecipe.setRemarks("从 " + source.getRecipeCode() + " 复制");
        newRecipe.setStatus(MixRecipe.RecipeStatus.PENDING_APPROVAL);

        // 复制材料清单
        for (MixRecipeItem sourceItem : source.getItems()) {
            MixRecipeItem newItem = new MixRecipeItem();
            newItem.setMaterial(sourceItem.getMaterial());
            newItem.setDosagePerM3(sourceItem.getDosagePerM3());
            newItem.setRemarks(sourceItem.getRemarks());

            newRecipe.addItem(newItem);
        }

        MixRecipe saved = mixRecipeRepository.save(newRecipe);
        return ApiResponse.success("配比复制成功", toDetailDTO(saved));
    }

    // DTO 转换方法

    private MixRecipeListDTO toListDTO(MixRecipe recipe) {
        MixRecipeListDTO dto = new MixRecipeListDTO();
        dto.setId(recipe.getId());
        dto.setRecipeCode(recipe.getRecipeCode());
        dto.setStrengthGrade(recipe.getStrengthGrade());
        dto.setSlump(recipe.getSlump());
        dto.setStatus(recipe.getStatus());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setCreatedBy(recipe.getCreatedBy());
        return dto;
    }

    private MixRecipeDetailDTO toDetailDTO(MixRecipe recipe) {
        MixRecipeDetailDTO dto = new MixRecipeDetailDTO();
        dto.setId(recipe.getId());
        dto.setRecipeCode(recipe.getRecipeCode());
        dto.setStrengthGrade(recipe.getStrengthGrade());
        dto.setSlump(recipe.getSlump());
        dto.setTechnicalRequirements(recipe.getTechnicalRequirements());
        dto.setRemarks(recipe.getRemarks());
        dto.setStatus(recipe.getStatus());
        dto.setCreatedAt(recipe.getCreatedAt());
        dto.setCreatedBy(recipe.getCreatedBy());

        List<MixRecipeItemDTO> items = recipe.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }

    private MixRecipeItemDTO toItemDTO(MixRecipeItem item) {
        MixRecipeItemDTO dto = new MixRecipeItemDTO();
        dto.setId(item.getId());
        dto.setMaterialId(item.getMaterial().getId());
        dto.setMaterialCode(item.getMaterial().getMaterialCode());
        dto.setMaterialName(item.getMaterial().getDescription());
        dto.setMaterialUnit(item.getMaterial().getBaseUnit());
        dto.setDosagePerM3(item.getDosagePerM3());
        dto.setRemarks(item.getRemarks());
        return dto;
    }

    // DTOs

    @Data
    public static class MixRecipeListDTO {
        private Long id;
        private String recipeCode;
        private String strengthGrade;
        private String slump;
        private MixRecipe.RecipeStatus status;
        private LocalDateTime createdAt;
        private String createdBy;
    }

    @Data
    public static class MixRecipeDetailDTO {
        private Long id;
        private String recipeCode;
        private String strengthGrade;
        private String slump;
        private String technicalRequirements;
        private String remarks;
        private MixRecipe.RecipeStatus status;
        private LocalDateTime createdAt;
        private String createdBy;
        private List<MixRecipeItemDTO> items;
    }

    @Data
    public static class MixRecipeItemDTO {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private String materialUnit;
        private BigDecimal dosagePerM3;
        private String remarks;
    }

    @Data
    public static class CreateMixRecipeRequest {
        @NotBlank(message = "配比编号不能为空")
        private String recipeCode;

        @NotBlank(message = "强度等级不能为空")
        private String strengthGrade;

        private String slump;
        private String technicalRequirements;
        private String remarks;

        @NotEmpty(message = "材料清单不能为空")
        private List<MixRecipeItemRequest> items = new ArrayList<>();
    }

    @Data
    public static class UpdateMixRecipeRequest {
        private String slump;
        private String technicalRequirements;
        private String remarks;

        @NotEmpty(message = "材料清单不能为空")
        private List<MixRecipeItemRequest> items = new ArrayList<>();
    }

    @Data
    public static class MixRecipeItemRequest {
        @NotNull(message = "材料ID不能为空")
        private Long materialId;

        @NotNull(message = "单方用量不能为空")
        private BigDecimal dosagePerM3;

        private String remarks;
    }

    @Data
    public static class CopyRecipeRequest {
        private String newRecipeCode;
    }
}
