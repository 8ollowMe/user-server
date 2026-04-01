package com.followme.userserver.domain.entity;


import com.followMe.common.entity.BaseAudit;
import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "p_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("status != 'DELETED'")
public class User extends BaseAudit implements Persistable<UUID> {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String username;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "slack_id", nullable = false, length = 50)
    private String slackId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "vendor_id")
    private UUID vendorId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @PostPersist
    @PostLoad
    protected void markNotNew() {
        this.isNew = false;
    }

    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public void approve() {
        this.status = UserStatus.APPROVED;
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public void deleteUser(UUID deletedByUserId) {
        this.status = UserStatus.DELETED;
        super.softDelete(deletedByUserId); 
    }

    public void updateProfile(UserRequest.UpdateProfile request) {
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getAddress() != null) {
            this.address = request.getAddress();
        }
        if (request.getPhone() != null) {
            this.phone = request.getPhone();
        }
        if (request.getSlackId() != null) {
            this.slackId = request.getSlackId();
        }
    }

    public void deactivateAccount(UUID deleterUsername) {
        this.status = UserStatus.DELETED;

        this.softDelete(deleterUsername);
    }

}