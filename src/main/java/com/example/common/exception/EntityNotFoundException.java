package com.example.common.exception;

import java.io.Serializable;

/**
 * 用于表示数据库中指定 ID 的记录不存在的情况。
 */
public class EntityNotFoundException extends BusinessException {

    private static final String DEFAULT_MESSAGE_TEMPLATE = "找不到记录 - Entity: %s, ID: %s";
    private static final String ERROR_CODE = "entity-not-found";

    /**
     * @param entityName 实体名称
     * @param id         实体 ID
     */
    public EntityNotFoundException(String entityName, Serializable id) {
        super(ERROR_CODE, String.format(DEFAULT_MESSAGE_TEMPLATE, entityName, id));
    }

    /**
     * @param entityClass 实体类
     * @param id          实体 ID
     */
    public EntityNotFoundException(Class<?> entityClass, Serializable id) {
        super(ERROR_CODE, String.format(DEFAULT_MESSAGE_TEMPLATE, entityClass.getSimpleName(), id));
    }

    /**
     * @param entityClass 实体类
     * @param id          实体 ID
     * @param message     自定义错误消息
     */
    public EntityNotFoundException(Class<?> entityClass, Serializable id, String message) {
        super(ERROR_CODE, message);
    }
}