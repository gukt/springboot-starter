package com.example.service.base;

import com.example.common.exception.BusinessException;
import com.example.common.exception.EntityNotFoundException;
import com.example.domain.base.BaseEntity;
import jakarta.persistence.QueryHint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * AbstractService 是所有服务类的基类，提供通用的 CRUD 操作。
 *
 * <p>此抽象类通过自动注入相关的 {@link JpaRepository} 来管理实体对象的持久化操作。
 * 自动注入遵循 Spring Data JPA 的命名约定，即 Repository Bean 名称应为实体名称加上 "Repository"。
 *
 * <p>如果自动注入失败，可以手动注入 {@link JpaRepository}，示例如下：
 *
 * <pre>{@code
 * @Service
 * public class UserService extends AbstractService<User, Long> {
 *
 * private final UserRepository userRepository;
 *
 * @PostConstruct
 * public void init() {
 * super.setRepository(userRepository);
 * }
 *
 *     // 其他方法...
 * }
 * }</pre>
 *
 * <p>请确保手动注入的 Repository 不为空，并遵循命名约定，以便于维护和扩展。
 *
 * @param <T>  实体类型
 * @param <ID> 实体 ID 类型
 */
@Transactional(readOnly = true)
@Slf4j
public abstract class AbstractService<T extends BaseEntity<ID>, ID extends Serializable> implements BaseService<T, ID>, InitializingBean {

    /**
     * 实体类类型，用于泛型处理和元数据访问。
     */
    private final Class<T> entityClass;
    /**
     * 访问类型为 {@code T} 的实体对象的 {@link JpaRepository}。
     * 该字段通过 {@link #setRepository} 方法注入，子类可以选择自动注入或手动注入。
     *
     * @see AbstractSpecificationService#setRepository(JpaRepository)
     */
    protected JpaRepository<T, ID> repository;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 构造函数，通过反射获取实体类的类型。
     */
    @SuppressWarnings("unchecked")
    protected AbstractService() {
        // 通过反射获取泛型类型
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 处理遵循 Spring Data JPA 命名约定的 Repository Bean 的自动注入。
     * <p>
     * 自动注入机制尝试根据实体名称构建 Repository Bean 名称并从 Spring 容器中获取对应的 Repository Bean。
     * 如果自动注入失败，将记录错误日志并抛出 {@link IllegalStateException}。
     */
    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        // 如果子类已经通过其他方式设置了 repository，就不再自动注入
        if (repository != null) {
            return;
        }

        String repositoryBeanName = getRepositoryBeanName();

        try {
            this.repository = (JpaRepository<T, ID>) applicationContext.getBean(repositoryBeanName);

            // 自动注入成功后，调用 onRepositorySet 模板方法
            onRepositorySet(this.repository);

            log.debug("Successfully auto-injected '{}' into '{}'.", repositoryBeanName, getServiceName());
        } catch (BeansException e) {
            log.error("""
                    Failed to auto-inject '{}' into '{}'. 
                    Please ensure that the repository is correctly defined and follows the naming conventions. 
                    If auto-injection fails, consider manually injecting the repository.
                    """, repositoryBeanName, getServiceName(), e);
            throw new IllegalStateException("Failed to initialize the Repository of %s".formatted(getServiceName()), e);
        }
    }

    /**
     * 设置访问实体对象 T 的 {@link JpaRepository}，允许子类覆盖此方法来自定义注入逻辑。
     *
     * @param repository 访问实体对象 T 的 {@link JpaRepository}
     * @throws NullPointerException 如果传入的 repository 为 null
     * @see AbstractSpecificationService#setRepository(JpaRepository)
     */
    protected void setRepository(JpaRepository<T, ID> repository) {
        this.repository = Objects.requireNonNull(repository, String.format("[%s] repository must not be null.", getServiceName()));

        String repositoryName = getRepositoryName(repository);
        String proxyInfo = AopUtils.isAopProxy(repository) ? " (Proxy: " + repository.getClass().getSimpleName() + ")" : "";
        log.debug("Successfully injected '{}{}' into '{}'.", repositoryName, proxyInfo, getServiceName());

        // 注入成功后，调用 onRepositorySet 模板方法
        onRepositorySet(repository);
    }

    /**
     * 模板方法，供子类覆盖, 为子类提供扩展点。
     *
     * @param repository 注入的 repository
     */
    protected void onRepositorySet(JpaRepository<T, ID> repository) {
        // 默认空实现
    }

    /**
     * 获取当前 Service 的名称，通常为类名，如 UserService。
     *
     * @return 当前 Service 的 SimpleName
     */
    protected String getServiceName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 构造 Repository bean 名称，遵循 Spring Data 默认命名规则, 如 userRepository。
     */
    private String getRepositoryBeanName() {
        // 实体名称 + Repository
        String repositoryBeanName = getEntityName() + "Repository";
        // 首字母小写
        repositoryBeanName = Character.toLowerCase(repositoryBeanName.charAt(0)) + repositoryBeanName.substring(1);
        return repositoryBeanName;
    }

    /**
     * 获取 Repository 的实际类名，处理代理对象的情况。
     *
     * @param repository JpaRepository 实例
     * @return Repository 的实际类名
     */
    private String getRepositoryName(JpaRepository<T, ID> repository) {
        if (AopUtils.isAopProxy(repository)) {
            // 如果是代理对象，尝试获取目标对象
            try {
                Object target = ((Advised) repository).getTargetSource().getTarget();
                // 获取目标对象实现的接口中的第一个自定义接口
                Class<?>[] interfaces = target.getClass().getInterfaces();
                for (Class<?> cls : interfaces) {
                    if (!cls.getName().startsWith("org.springframework")) {
                        return cls.getSimpleName();
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to get target repository name", e);
            }
        }

        // 如果无法获取目标对象，尝试接口中获取
        Class<?>[] interfaces = repository.getClass().getInterfaces();
        for (Class<?> cls : interfaces) {
            if (!cls.getName().startsWith("org.springframework")) {
                return cls.getSimpleName();
            }
        }

        // 如果都失败了，返回类名
        return repository.getClass().getSimpleName();
    }

    /**
     * 获取实体类名称，用于构造 Repository Bean 名称或日志输出。
     *
     * @return 实体类的 SimpleName，如 User、Role 等
     */
    protected String getEntityName() {
        return entityClass.getSimpleName();
    }

    /**
     * 获取实体类的类型，用于泛型处理和元数据访问。
     *
     * @return 实体类的 Class 对象
     */
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public T getReferenceById(ID id) {
        return repository.getReferenceById(id);
    }

    @Override
    public T getById(ID id) throws EntityNotFoundException {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(this.getEntityName(), id));
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("根据 ID 查找实体 - {}, id: {}", getEntityName(), id);
        Optional<T> result = repository.findById(id);
        log.debug("查找到的实体 - {}, id: {}, result: {}", getEntityName(), id, result.orElse(null));
        return result;
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        log.debug("正在批量查询指 ID 集合实体 - {}, ids: {}", getEntityName(), ids);
        return repository.findAllById(ids);
    }

    @Override
    public List<T> findAllByExample(T example) {
        return findAllByExample(example, ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase());
    }

    @Override
    public List<T> findAllByExample(T example, ExampleMatcher matcher) {
        log.debug("""
                正在使用示例对象进行查询 -
                entity: {}
                example: {}
                matcher: {}""", getEntityName(), example, matcher);
        return repository.findAll(Example.of(example, matcher));
    }

    @Override
    public List<T> findAllByExample(T example, Sort sort) {
        return findAllByExample(example, sort, ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase());
    }

    @Override
    public List<T> findAllByExample(T example, Sort sort, ExampleMatcher matcher) {
        log.debug("""
                正在使用 Example 对象进行查询并排序 - 
                example: {}
                sort: {}
                matcher: {}""", example, sort, matcher);
        return repository.findAll(Example.of(example, matcher), sort);
    }

    @Override
    public Page<T> findAllByExample(T example, Pageable pageable) {
        log.debug("正在使用 Example 对象进行分页查询 - {}", example);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
        return repository.findAll(Example.of(example, matcher), pageable);
    }

    @Override
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    public List<T> findAll() {
        log.debug("正在查询所有实体 - {}", getEntityName());
        return repository.findAll();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        log.debug("正在分页查询所有实体 - {}, pageable: {}", getEntityName(), pageable);
        return repository.findAll(pageable);
    }


    @Override
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    public List<T> findAll(Sort sort) {
        log.debug("正在查询所有实体并排序 - {}, sort: {}", getEntityName(), sort);
        return repository.findAll(sort);
    }

    @Override
    @Transactional
    public T save(T entity) {
        log.debug("正在保存实体对象 - {}", entity);
        try {
            T savedEntity = repository.save(entity);
            log.info("成功保存实体对象 - {}, ID: {}", getEntityName(), savedEntity.getId());
            return savedEntity;
        } catch (Exception e) {
            throw wrapException("保存实体对象", e);
        }
    }

    @Override
    @Transactional
    public T saveAndFlush(T entity) {
        log.debug("正在保存实体对象并立即刷新 - {}", entity);
        try {
            T savedEntity = repository.saveAndFlush(entity);
            log.info("成功保存实体对象并立即刷新 - {}, ID: {}", getEntityName(), savedEntity.getId());
            return savedEntity;
        } catch (Exception e) {
            log.error("保存实体对象并立即刷新失败 - {}", entity, e);
            throw wrapException("保存实体对象并立即刷新", e);
        }
    }

    @Override
    @Transactional
    public List<T> saveAll(Iterable<T> entities) {
        log.debug("正在批量保存实体对象 - {}", getEntityName());
        try {
            List<T> savedEntities = repository.saveAll(entities);
            log.info("成功批量保存 {} 个实体对象 - {}", savedEntities.size(), getEntityName());
            return savedEntities;
        } catch (Exception e) {
            throw wrapException("批量保存实体对象", e);
        }
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        log.debug("正在删除实体对象 - {}, ID: {}", getEntityName(), id);
        try {
            repository.deleteById(id);
            log.info("成功删除实体对象 - {}, ID: {}", getEntityName(), id);
        } catch (Exception e) {
            throw wrapException("删除实体对象", e);
        }
    }

    @Override
    @Transactional
    public void delete(T entity) {
        log.debug("正在删除实体对象 - {}", entity);
        try {
            repository.delete(entity);
            log.info("成功删除实体对象 - {}", entity);
        } catch (Exception e) {
            throw wrapException("删除实体对象", e);
        }
    }

    @Override
    @Transactional
    public void deleteAll(Iterable<? extends T> entities) {
        log.debug("正在删除实体对象列表: {}", entities);
        try {
            repository.deleteAll(entities);
            log.info("成功删除实体对象列表: {}", entities);
        } catch (Exception e) {
            throw wrapException("删除实体对象列表", e);
        }
    }

    @Override
    public boolean exists(ID id) {
        log.debug("正在检查实体是否存在 - {}, ID: {}", getEntityName(), id);
        return repository.existsById(id);
    }

    @Override
    public long count() {
        log.debug("正在统计实体数量 - {}", getEntityName());
        return repository.count();
    }

    // === Helper Methods ===

    /**
     * 包装异常
     *
     * @param operation 操作名称
     * @param e         原始异常
     * @return 包装后的异常
     */
    protected RuntimeException wrapException(String operation, Exception e) {
        if (e instanceof DataIntegrityViolationException) {
            return new BusinessException("数据约束冲突: " + e.getMessage(), e);
        }
        return new BusinessException(operation + "失败: " + e.getMessage(), e);
    }

    private String getRepositoryName(Object repository) {
        if (repository == null) {
            return "null";
        }

        try {
            // 处理 JDK 动态代理
            if (AopUtils.isJdkDynamicProxy(repository)) {
                return ((Advised) repository).getTargetClass().getSimpleName();
            }

            // 处理 CGLIB 代理
            if (AopUtils.isCglibProxy(repository)) {
                Class<?> targetClass = AopUtils.getTargetClass(repository);
                return targetClass.getSimpleName();
            }

            // 非代理对象
            return repository.getClass().getSimpleName();

        } catch (Exception e) {
            log.warn("获取 repository 名称失败", e);
            return repository.getClass().getSimpleName();
        }
    }
}