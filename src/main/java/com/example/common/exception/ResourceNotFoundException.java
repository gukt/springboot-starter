package com.example.common.exception;

/**
 * 资源不存在异常
 * 用于处理请求的资源不存在的场景
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 资源类型
     */
    private final String resourceType;

    /**
     * 资源ID
     */
    private final String resourceId;

    /**
     * 构造方法
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Unknown";
        this.resourceId = null;
    }

    /**
     * 构造方法
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * 构造方法
     */
    public ResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * 构造方法
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.resourceType = "Unknown";
        this.resourceId = null;
    }

    /**
     * 创建用户不存在异常
     */
    public static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("User", userId.toString(), "用户不存在");
    }

    /**
     * 创建用户不存在异常（通过用户名）
     */
    public static ResourceNotFoundException userNotFound(String username) {
        return new ResourceNotFoundException("User", username, "用户不存在");
    }

    /**
     * 创建角色不存在异常
     */
    public static ResourceNotFoundException roleNotFound(Long roleId) {
        return new ResourceNotFoundException("Role", roleId.toString(), "角色不存在");
    }

    /**
     * 创建权限不存在异常
     */
    public static ResourceNotFoundException permissionNotFound(Long permissionId) {
        return new ResourceNotFoundException("Permission", permissionId.toString(), "权限不存在");
    }

    /**
     * 创建配置不存在异常
     */
    public static ResourceNotFoundException configNotFound(String configKey) {
        return new ResourceNotFoundException("Config", configKey, "配置不存在");
    }

    /**
     * 创建文件不存在异常
     */
    public static ResourceNotFoundException fileNotFound(String fileName) {
        return new ResourceNotFoundException("File", fileName, "文件不存在");
    }

    /**
     * 创建目录不存在异常
     */
    public static ResourceNotFoundException directoryNotFound(String directoryName) {
        return new ResourceNotFoundException("Directory", directoryName, "目录不存在");
    }

    /**
     * 创建订单不存在异常
     */
    public static ResourceNotFoundException orderNotFound(String orderNumber) {
        return new ResourceNotFoundException("Order", orderNumber, "订单不存在");
    }

    /**
     * 创建产品不存在异常
     */
    public static ResourceNotFoundException productNotFound(Long productId) {
        return new ResourceNotFoundException("Product", productId.toString(), "产品不存在");
    }

    /**
     * 创建分类不存在异常
     */
    public static ResourceNotFoundException categoryNotFound(Long categoryId) {
        return new ResourceNotFoundException("Category", categoryId.toString(), "分类不存在");
    }

    /**
     * 创建评论不存在异常
     */
    public static ResourceNotFoundException commentNotFound(Long commentId) {
        return new ResourceNotFoundException("Comment", commentId.toString(), "评论不存在");
    }

    /**
     * 创建消息不存在异常
     */
    public static ResourceNotFoundException messageNotFound(Long messageId) {
        return new ResourceNotFoundException("Message", messageId.toString(), "消息不存在");
    }

    /**
     * 创建通知不存在异常
     */
    public static ResourceNotFoundException notificationNotFound(Long notificationId) {
        return new ResourceNotFoundException("Notification", notificationId.toString(), "通知不存在");
    }

    /**
     * 创建任务不存在异常
     */
    public static ResourceNotFoundException taskNotFound(Long taskId) {
        return new ResourceNotFoundException("Task", taskId.toString(), "任务不存在");
    }

    /**
     * 创建项目不存在异常
     */
    public static ResourceNotFoundException projectNotFound(Long projectId) {
        return new ResourceNotFoundException("Project", projectId.toString(), "项目不存在");
    }

    /**
     * 创建部门不存在异常
     */
    public static ResourceNotFoundException departmentNotFound(Long departmentId) {
        return new ResourceNotFoundException("Department", departmentId.toString(), "部门不存在");
    }

    /**
     * 创建员工不存在异常
     */
    public static ResourceNotFoundException employeeNotFound(Long employeeId) {
        return new ResourceNotFoundException("Employee", employeeId.toString(), "员工不存在");
    }

    /**
     * 创建客户不存在异常
     */
    public static ResourceNotFoundException customerNotFound(Long customerId) {
        return new ResourceNotFoundException("Customer", customerId.toString(), "客户不存在");
    }

    /**
     * 创建供应商不存在异常
     */
    public static ResourceNotFoundException supplierNotFound(Long supplierId) {
        return new ResourceNotFoundException("Supplier", supplierId.toString(), "供应商不存在");
    }

    /**
     * 创建仓库不存在异常
     */
    public static ResourceNotFoundException warehouseNotFound(Long warehouseId) {
        return new ResourceNotFoundException("Warehouse", warehouseId.toString(), "仓库不存在");
    }

    /**
     * 创建库存不存在异常
     */
    public static ResourceNotFoundException inventoryNotFound(Long inventoryId) {
        return new ResourceNotFoundException("Inventory", inventoryId.toString(), "库存不存在");
    }

    /**
     * 创建物流订单不存在异常
     */
    public static ResourceNotFoundException logisticsNotFound(String trackingNumber) {
        return new ResourceNotFoundException("Logistics", trackingNumber, "物流订单不存在");
    }

    /**
     * 获取资源类型
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * 获取资源ID
     */
    public String getResourceId() {
        return resourceId;
    }
}