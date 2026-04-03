package com.followme.userserver.domain.repository;

import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    List<User> findAllByHubIdAndRole(UUID hubId, UserRole role);

    List<User> findAllByHubIdAndRoleOrderBySequenceAsc(UUID hubId, UserRole role);
    List<User> findAllByVendorIdAndRoleOrderBySequenceAsc(UUID vendorId, UserRole role);

    @Query("SELECT COALESCE(MAX(u.sequence), 0L) FROM User u WHERE u.hubId = :hubId AND u.role = :role")
    Long findMaxSequenceByHubIdAndRole(@Param("hubId") UUID hubId, @Param("role") UserRole role);

    @Query("SELECT COALESCE(MAX(u.sequence), 0L) FROM User u WHERE u.vendorId = :vendorId AND u.role = :role")
    Long findMaxSequenceByVendorIdAndRole(@Param("vendorId") UUID vendorId, @Param("role") UserRole role);
}