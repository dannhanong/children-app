package com.team.child_be.repositories;

import com.team.child_be.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    User findByUsername(String username);
    User findByVerificationCode(String verificationCode);
    User findByResetPasswordToken(String resetPasswordToken);
    User findByResetPasswordTokenAndDeletedAtNull(String resetPasswordToken);

    User findByVerificationCodeAndDeletedAtNull(String code);

    List<User> findAllByCreatedAtBeforeAndEnabled(LocalDateTime twoDaysAgo, boolean enabled);

    @Query("SELECT u FROM User u WHERE CONCAT(u.name, ' ', u.username) LIKE %:keyword%")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE CONCAT(u.name, ' ', u.username) LIKE %:keyword%")
    List<User> lSearchByKeyword(@Param("keyword") String keyword);

    boolean existsByPhoneNumberAndUsernameNot(String phoneNumber, String username);

    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByAccessCode(String accessCode);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.parentId = :parentId AND r.name = com.team.child_be.dtos.enums.RoleName.CHILD AND u.deletedAt IS NULL")
    List<User> findChildrenByParentId(@Param("parentId") Long parentId);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.id = :id AND u.parentId = :parentId AND r.name = com.team.child_be.dtos.enums.RoleName.CHILD AND u.deletedAt IS NULL")
    Optional<User> findChildByIdAndParentId(@Param("id") Long id, @Param("parentId") Long parentId);

    Optional<User> findByAccessCodeAndDeletedAtIsNull(String accessCode);
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.parentId = :parentId AND r.name = com.team.child_be.dtos.enums.RoleName.CHILD AND u.deletedAt IS NULL AND LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchChildrenByNameForParent(@Param("parentId") Long parentId, @Param("keyword") String keyword);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.parentId = :parentId AND r.name = com.team.child_be.dtos.enums.RoleName.CHILD AND u.deletedAt IS NULL")
    long countChildrenByParent(@Param("parentId") Long parentId);
}
