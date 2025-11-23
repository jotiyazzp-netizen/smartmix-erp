package com.company.smartmix.cost;

import com.company.smartmix.common.ApiResponse;
import com.company.smartmix.common.BusinessException;
import com.company.smartmix.material.MaterialPrice;
import com.company.smartmix.material.MaterialPriceRepository;
import com.company.smartmix.mix.MixRecipe;
import com.company.smartmix.mix.MixRecipeItem;
import com.company.smartmix.mix.MixRecipeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成本优化推荐引擎控制器
 */
@Tag(name = "成本优化", description = "成本计算与优化推荐")
@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
public class CostOptimizationController {

    private final MixRecipeRepository mixRecipeRepository;
    private final MaterialPriceRepository materialPriceRepository;

    @Operation(summary = "获取成本优化推荐", description = "根据强度等级和方量推荐最低成本配比")
    @GetMapping("/recommendations")
    public ApiResponse<List<CostRecommendationDTO>> getRecommendations(
            @RequestParam String strengthGrade,
            @RequestParam BigDecimal volume) {

        if (volume == null || volume.compareTo(BigDecimal.ZERO) <= 0) {
            return ApiResponse.badRequest("方量必须大于0");
        }

        // 查询符合强度等级且已审核的配比
        List<MixRecipe> approvedRecipes = mixRecipeRepository.findByStrengthGradeAndStatus(
                strengthGrade, MixRecipe.RecipeStatus.APPROVED);

        if (approvedRecipes.isEmpty()) {
            return ApiResponse.success("没有找到符合条件的已审核配比", new ArrayList<>());
        }

        // 计算每个配比的成本
        List<CostRecommendationDTO> recommendations = new ArrayList<>();

        for (MixRecipe recipe : approvedRecipes) {
            try {
                CostRecommendationDTO recommendation = calculateCost(recipe, volume);
                if (recommendation != null) {
                    recommendations.add(recommendation);
                }
            } catch (Exception e) {
                // 跳过计算失败的配比
                continue;
            }
        }

        if (recommendations.isEmpty()) {
            return ApiResponse.success("所有配比的材料价格数据不完整", new ArrayList<>());
        }

        // 按单方成本排序
        recommendations.sort(Comparator.comparing(CostRecommendationDTO::getUnitCost));

        // 标记最低成本配比
        if (!recommendations.isEmpty()) {
            recommendations.get(0).setBest(true);
        }

        return ApiResponse.success(recommendations);
    }

    /**
     * 计算单个配比的成本
     */
    private CostRecommendationDTO calculateCost(MixRecipe recipe, BigDecimal volume) {
        CostRecommendationDTO dto = new CostRecommendationDTO();
        dto.setMixRecipeId(recipe.getId());
        dto.setMixRecipeCode(recipe.getRecipeCode());
        dto.setStrengthGrade(recipe.getStrengthGrade());
        dto.setSlump(recipe.getSlump());

        List<MaterialCostDetail> materialDetails = new ArrayList<>();
        BigDecimal unitCost = BigDecimal.ZERO;
        boolean priceIncomplete = false;

        // 计算每种材料的成本
        for (MixRecipeItem item : recipe.getItems()) {
            MaterialPrice currentPrice = materialPriceRepository
                    .findByMaterialAndIsCurrent(item.getMaterial(), true)
                    .orElse(null);

            if (currentPrice == null || currentPrice.getPricePerKg() == null) {
                priceIncomplete = true;
                break;
            }

            BigDecimal dosage = item.getDosagePerM3();
            BigDecimal unitPrice = currentPrice.getPricePerKg();
            BigDecimal costPerM3 = dosage.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

            MaterialCostDetail detail = new MaterialCostDetail();
            detail.setMaterialCode(item.getMaterial().getMaterialCode());
            detail.setMaterialName(item.getMaterial().getDescription());
            detail.setDosagePerM3(dosage);
            detail.setUnitPrice(unitPrice);
            detail.setCostPerM3(costPerM3);

            materialDetails.add(detail);
            unitCost = unitCost.add(costPerM3);
        }

        // 如果价格不完整，返回 null
        if (priceIncomplete) {
            return null;
        }

        dto.setUnitCost(unitCost.setScale(2, RoundingMode.HALF_UP));
        dto.setTotalCost(unitCost.multiply(volume).setScale(2, RoundingMode.HALF_UP));
        dto.setMaterialDetails(materialDetails);
        dto.setPriceIncomplete(false);
        dto.setBest(false);

        return dto;
    }

    // DTOs

    @Data
    public static class CostRecommendationDTO {
        private Long mixRecipeId;
        private String mixRecipeCode;
        private String strengthGrade;
        private String slump;
        private BigDecimal unitCost; // 单方成本（元/m³）
        private BigDecimal totalCost; // 总成本（元）
        private boolean isBest; // 是否最低成本推荐
        private boolean priceIncomplete; // 价格数据是否不完整
        private List<MaterialCostDetail> materialDetails;
    }

    @Data
    public static class MaterialCostDetail {
        private String materialCode;
        private String materialName;
        private BigDecimal dosagePerM3; // 单方用量（kg/m³）
        private BigDecimal unitPrice; // 单价（元/kg）
        private BigDecimal costPerM3; // 单方成本（元/m³）
    }
}
