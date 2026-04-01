package com.followme.userserver.application.mapper;

import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "status", constant = "PENDING")
    User toEntity(UserRequest.Register request);

    @Mapping(source = "id", target = "userId")
    UserResponse.Info toResponseDto(User user);
    
    @Mapping(source = "id", target = "userId")
    UserResponse.Internal toInternalResponseDto(User user);
}