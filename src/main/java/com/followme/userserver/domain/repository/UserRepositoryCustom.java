package com.followme.userserver.domain.repository;

import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserRepositoryCustom {
    Page<User> searchUsers(UserRequest.SearchCondition condition, Pageable pageable);
    List<User> findManagersByHubId(UUID hubId);
    List<User> findDeliveries(UUID hubId, UUID vendorId);
    Long findMaxSequence(UUID hubId, UserRole role);
}