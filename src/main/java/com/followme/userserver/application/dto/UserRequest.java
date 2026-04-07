package com.followme.userserver.application.dto;

import java.util.UUID;

import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public class UserRequest {

        @Getter
        public static class Register {
        @NotBlank
        @Pattern(regexp = "^[a-z0-9]{4,10}$")
        private String username;

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$")
        private String password;

        @NotBlank
        private String name;

        private String address;
        
        private String phone;

        @NotBlank
        private String slackId;

        @NotNull
        private UserRole role;

        private UUID hubId;
        
        private UUID vendorId;

        @AssertTrue(message = "소속 업체(vendorId) 또는 허브(hubId) 중 하나는 필수 입력값입니다.")
        public boolean isAffiliationValid() {
            if (this.role == UserRole.MASTER) {
                return true; 
            }
            return this.hubId != null || this.vendorId != null;
        }
    }

    

    @Getter
    public static class UpdateProfile {
        private String name;
        private String address;
        private String phone;
        private String slackId;
    }


    @Getter
    public static class UpdateStatus {
        private UserStatus status;
    }

    @Getter
    @Setter
    public static class SearchCondition {
        private UserRole role;
        private UserStatus status;
        private String keyword;
    }
}