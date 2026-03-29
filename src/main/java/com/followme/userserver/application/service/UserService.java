package com.followme.userserver.application.service;

import com.followme.userserver.application.dto.UserRegisterRequestDto;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserStatus;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void registerUser(UserRegisterRequestDto request) {
        
        // 1. 아이디 중복 검증 
        if (userRepository.existsByUsername(request.getUsername())) {
            throw UserErrorCode.DUPLICATE_USERNAME.toException(); 
        }

        // 2. DTO 데이터를 바탕으로 엔티티 조립
        User newUser = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .slackId(request.getSlackId())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .hubId(request.getHubId())
                .vendorId(request.getVendorId())
                .build();

        // 3. DB에 저장
        userRepository.save(newUser);
    }
}