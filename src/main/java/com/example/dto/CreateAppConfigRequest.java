package com.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建应用配置请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建应用配置请求")
public class CreateAppConfigRequest {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 100, message = "配置键长度不能超过 100 个字符")
    @Schema(description = "配置键", example = "app.name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    @Schema(description = "配置值", example = "Spring Boot Starter", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configValue;

    @Schema(description = "配置组", example = "app")
    private String configGroup = "default";

    @Schema(description = "值类型", example = "string")
    private String valueType = "string";

    @Size(max = 255, message = "描述长度不能超过 255 个字符")
    @Schema(description = "配置描述", example = "应用名称")
    private String description;

    @Schema(description = "是否系统配置", example = "false")
    private Boolean isSystem = false;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean isEditable = true;
}