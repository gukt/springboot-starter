package com.example.repository;

import com.example.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 查找所有管理员用户
     */
    List<User> findByIsAdminTrue();

    /**
     * 根据状态查找用户
     */
    Page<User> findByStatus(Boolean status, Pageable pageable);

    /**
     * 查找指定角色的用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.code = :roleCode")
    Page<User> findByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);

    /**
     * 查找指定权限的用户
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r JOIN r.permissions p WHERE p.code = :permissionCode")
    Page<User> findByPermissionCode(@Param("permissionCode") String permissionCode, Pageable pageable);

    /**
     * 查找最近登录的用户
     */
    List<User> findTop10ByLastLoginAtIsNotNullOrderByLastLoginAtDesc();

    /**
     * 查找指定时间段内创建的用户
     */
    List<User> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户数量
     */
    long countByStatus(Boolean status);

    /**
     * 搜索用户（支持用户名、邮箱、全名模糊搜索）
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(@Param("keyword") String keyword, @Param("status") Boolean status, Pageable pageable);

    /**
     * 根据角色ID查找用户
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    Page<User> findByRoleId(@Param("roleId") Long roleId, Pageable pageable);

    /**
     * 查找需要更新密码的用户（密码过期）
     */
    @Query("SELECT u FROM User u WHERE u.passwordUpdatedAt < :expiryDate")
    List<User> findUsersWithExpiredPassword(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * 查找长时间未登录的用户
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :inactiveDate AND u.status = true")
    List<User> findInactiveUsers(@Param("inactiveDate") LocalDateTime inactiveDate);

    /**
     * 批量更新用户状态
     */
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :userIds")
    int updateStatusByIds(@Param("userIds") List<Long> userIds, @Param("status") Boolean status);

    /**
     * 删除用户角色关联
     */
    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.userId = :userId")
    void deleteUserRolesByUserId(@Param("userId") Long userId);

    /**
     * 统计每月新增用户数
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') as month, COUNT(u) as count " +
           "FROM User u WHERE u.createdAt >= :startDate GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') " +
           "ORDER BY month")
    List<Object[]> countUsersByMonth(@Param("startDate") LocalDateTime startDate);
}