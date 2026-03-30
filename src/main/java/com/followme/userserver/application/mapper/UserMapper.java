package com.followme.userserver.application.mapper;

import com.followme.userserver.application.dto.UserRegisterRequestDto;
import com.followme.userserver.application.dto.UserResponseDto; // 💡 DTO import 추가
import com.followme.userserver.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "status", constant = "PENDING")
    User toEntity(UserRegisterRequestDto request);

    @Mapping(source = "id", target = "userId")
    UserResponseDto toResponseDto(User user);
    
}