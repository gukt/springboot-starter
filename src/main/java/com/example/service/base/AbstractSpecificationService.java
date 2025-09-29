package com.example.service.base;

import com.poetry.domain.base.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * 支持 JPA Specification 查询的抽象服务类。
 *
 * <p>此类扩展了 {@link AbstractService}，添加了对 JPA Specification 的支持，
 * 允许进行动态条件查询。要使用此类，Repository 必须同时实现 {@link JpaRepository}
 * 和 {@link JpaSpecificationExecutor} 接口。
 *
 * <p>使用示例：
 * <pre>{@code
 * @Service
 * public class UserService extends AbstractSpecificationService<User, Long> {
 *     private final UserRepository userRepository;
 *
 *     public UserService(UserRepository userRepository) {
 *         super.setRepository(userRepository);
 *         this.userRepository = userRepository;
 *     }
 *
 *     public Page<User> findActiveUsers(Pageable pageable) {
 *         return findByCondition(
 *             (root, query, cb) -> cb.equal(root.get("active"), true),
 *             pageable
 *         );
 *     }
 * }
 * }</pre>
 *
 * @param <T>  实体类型，必须继承自 {@link BaseEntity}
 * @param <ID> 实体 ID 类型
 * @see AbstractService
 * @see JpaSpecificationExecutor
 */
@Slf4j
public abstract class AbstractSpecificationService<T extends BaseEntity<ID>, ID>
        extends AbstractService<T, ID> {

    /**
     * JPA Specification 执行器，用于支持动态条件查询。
     * 在 {@link #setRepository} 方法中初始化。
     */
    protected JpaSpecificationExecutor<T> specificationExecutor;

    // @Override
    // @SuppressWarnings("unchecked")
    // protected void setRepository(JpaRepository<T, ID> repository) {
    //     super.setRepository(repository);

    //     if (!(repository instanceof JpaSpecificationExecutor)) {
    //         throw new IllegalArgumentException(
    //             String.format("Repository [%s] must implement JpaSpecificationExecutor for %s",
    //                 repository.getClass().getSimpleName(),
    //                 this.getClass().getSimpleName())
    //         );
    //     }

    //     this.specificationExecutor = (JpaSpecificationExecutor<T>) repository;
    //     log.debug("Successfully initialized JpaSpecificationExecutor for {}", getServiceName());
    // }

    @Override
    @SuppressWarnings("unchecked")
    protected void onRepositorySet(JpaRepository<T, ID> repository) {
        if (!(repository instanceof JpaSpecificationExecutor)) {
            throw new IllegalArgumentException(
                    String.format("Repository [%s] must implement JpaSpecificationExecutor for %s",
                            repository.getClass().getSimpleName(),
                            this.getClass().getSimpleName()));
        }

        this.specificationExecutor = (JpaSpecificationExecutor<T>) repository;
        log.debug("Successfully initialized JpaSpecificationExecutor for {}", getServiceName());
    }

    /**
     * 使用条件查询并分页获取实体列表。
     *
     * @param spec     查询条件，可以为 null
     * @param pageable 分页参数，不能为 null
     * @return 分页后的实体列表
     * @throws IllegalArgumentException 如果 pageable 为 null
     */
    public Page<T> findByCondition(Specification<T> spec, Pageable pageable) {
        log.debug("正在使用条件查询 - {}, spec: {}, pageable: {}", getEntityName(), spec, pageable);
        return specificationExecutor.findAll(spec, pageable);
    }

    /**
     * 根据条件统计实体数量。
     *
     * @param spec 查询条件，可以为 null
     * @return 符合条件的实体数量
     */
    public long countByCondition(Specification<T> spec) {
        log.debug("正在根据条件统计实体数量 - {}, spec: {}", getEntityName(), spec);
        return specificationExecutor.count(spec);
    }

    /**
     * 使用条件查询单个实体。
     *
     * @param spec 查询条件，不能为 null
     * @return 符合条件的实体，如果没有找到则返回 empty
     * @throws IllegalArgumentException 如果 spec 为 null
     */
    public Optional<T> findOne(Specification<T> spec) {
        log.debug("正在使用条件查询单个实体 - {}, spec: {}", getEntityName(), spec);
        return specificationExecutor.findOne(spec);
    }

    /**
     * 使用条件查询所有符合条件的实体。
     *
     * @param spec 查询条件，可以为 null
     * @return 符合条件的实体列表
     */
    public List<T> findAll(Specification<T> spec) {
        log.debug("正在使用条件查询所有实体 - {}, spec: {}", getEntityName(), spec);
        return specificationExecutor.findAll(spec);
    }
}