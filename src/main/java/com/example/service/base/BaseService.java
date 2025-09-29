package com.example.service.base;

import com.example.domain.base.BaseEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 服务基类。
 * <p>
 * 1. 所有查询方法自动继承只读事务
 * 2. 所有写操作方法自动继承读写事务
 * <p>
 * 重要注意事项：
 * <ul>
 * <li>使用 JDK 动态代理时（默认情况），接口上的事务注解会生效</li>
 * <li>使用 CGLIB 代理时（没有接口的类），接口上的事务注解会被忽略</li>
 * </ul>
 */
@Transactional(readOnly = true) // 类级别设置默认只读
public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {

    /**
     * 根据 ID 获取实体对象的引用。
     *
     * @param id 实体对象的 ID
     * @return 实体对象的引用
     */
    T getReferenceById(ID id);

    /**
     * 根据 ID 获取实体对象。
     * 如果对象不存在，则抛出 EntityNotFoundException 异常。
     *
     * @param id 实体对象的 ID
     * @return 实体对象
     * @throws EntityNotFoundException 如果对象不存在
     */
    T getById(ID id) throws EntityNotFoundException;

    /**
     * 根据 ID 查找实体对象。
     * 该方法会返回一个 Optional 对象，包含了实体对象或者 null。
     * 如果需要抛出异常，请使用 getById 方法。
     *
     * @param id 实体对象的 ID
     * @return Optional 对象，包含了实体对象或者 null
     */
    Optional<T> findById(ID id);

    /**
     * 批量查询指定 ID 列表的实体
     */
    List<T> findAllById(Iterable<ID> ids);

    /**
     * 查找所有实体对象。
     *
     * @return 包含所有实体对象的列表
     */
    List<T> findAll();

    /**
     * 查询所有实体并按指定字段排序。
     *
     * <pre>
     * // 单字段排序
     * Sort sort = Sort.by("id").descending();
     * List<Entity> entities = service.findAll(sort);
     *
     * // 多字段排序
     * Sort multiSort = Sort.by(
     *         Sort.Order.desc("id"),
     *         Sort.Order.asc("name"));
     * List<Entity> entities = service.findAll(multiSort);
     * </pre>
     *
     * @param sort 排序参数
     * @return 排序后的实体对象列表
     */
    List<T> findAll(Sort sort);

    /**
     * 分页查找所有实体对象。
     *
     * @param pageable 分页信息
     * @return 包含分页实体对象的分页对象
     */
    Page<T> findAll(Pageable pageable);

    /**
     * 根据示例对象查询实体对象列表
     *
     * @param example 示例对象
     * @return 包含实体对象的列表
     */
    List<T> findAllByExample(T example);

    /**
     * 根据示例对象查询实体对象列表并按指定字段排序。
     *
     * @param example 示例对象
     * @param sort    排序参数
     * @return 包含实体对象的列表
     */
    List<T> findAllByExample(T example, Sort sort);

    Page<T> findAllByExample(T example, Pageable pageable);

    /**
     * 使用自定义的 ExampleMatcher 根据示例对象查询并排序
     *
     * @param example 示例对象，包含查询条件
     * @param sort 排序条件
     * @param matcher 自定义的匹配器，用于配置字符串匹配策略和大小写敏感性等
     * @return 匹配的实体对象列表
     */
    List<T> findAllByExample(T example, Sort sort, ExampleMatcher matcher);

    /**
     * 使用自定义的 ExampleMatcher 根据示例对象查询
     *
     * @param example 示例对象，包含查询条件
     * @param matcher 自定义的匹配器，用于配置字符串匹配策略和大小写敏感性等
     * @return 匹配的实体对象列表
     */
    List<T> findAllByExample(T example, ExampleMatcher matcher);

    /**
     * 保存实体对象。
     * 该方法在事务中执行，确保数据的一致性。
     *
     * @param entity 要保存的实体对象
     * @return 保存后的实体对象
     */
    @Transactional
    T save(T entity);

    /**
     * 保存实体对象并立即刷新。
     *
     * @param entity 要保存的实体对象
     * @return 保存后的实体对象
     */
    @Transactional
    T saveAndFlush(T entity);

    /**
     * 批量保存多个实体对象。
     *
     * @param entities 要保存的实体对象列表
     * @return 保存后的实体对象列表
     */
    @Transactional
    List<T> saveAll(Iterable<T> entities);

    /**
     * 根据 ID 删除实体对象。
     * 该方法在事务中执行，确保数据的一致性。
     *
     * @param id 要删除的实体对象的 ID
     */
    @Transactional
    void deleteById(ID id);

    /**
     * 删除指定的实体对象。
     *
     * @param entity 要删除的实体对象
     */
    @Transactional
    void delete(T entity);

    /**
     * 删除指定的实体对象列表。
     *
     * @param entities 要删除的实体对象列表
     */
    @Transactional
    void deleteAll(Iterable<? extends T> entities);

    /**
     * 检查实体是否存在
     */
    boolean exists(ID id);

    /**
     * 统计实体数量
     */
    long count();

}
