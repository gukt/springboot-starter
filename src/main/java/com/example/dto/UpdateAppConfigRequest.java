package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 更新应用配置请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新应用配置请求")
public class UpdateAppConfigRequest {

    @NotBlank(message = "配置值不能为空")
    @Schema(description = "配置值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configValue;

    @Size(max = 255, message = "描述长度不能超过 255 个字符")
    @Schema(description = "配置描述")
    private String description;

    @Schema(description = "是否可编辑")
    private Boolean isEditable;
}