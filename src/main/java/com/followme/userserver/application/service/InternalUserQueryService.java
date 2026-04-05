package com.followme.userserver.application.service;

import com.followme.userserver.application.dto.UserInternalResponseDto;
import com.followme.userserver.application.mapper.UserMapper;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalUserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserInternalResponseDto getUserInternal(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMapper.toInternalResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserInternalResponseDto> getUsersInternal(List<UUID> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(userMapper::toInternalResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getUserSlackId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getSlackId();
    }

    @Transactional(readOnly = true)
    public List<UserInternalResponseDto> getManagersByHub(UUID hubId) {
        List<User> managers = userRepository.findAllByHubIdAndRole(hubId, UserRole.HUB);
        return managers.stream()
                .map(userMapper::toInternalResponseDto)
                .toList();
    }
}