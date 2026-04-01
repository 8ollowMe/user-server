package com.followme.userserver.application.service;

import com.followMe.common.pagination.PageResponse;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.application.mapper.UserMapper;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse.Info getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.Info getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse.Info> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return PageResponse.of(userPage, userMapper::toResponseDto);
    }
}