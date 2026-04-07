package com.followme.userserver.domain.event;

import com.followMe.common.event.BaseEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class UserStatusUpdatedEvent extends BaseEvent {
    private final String newStatus;

    public UserStatusUpdatedEvent(UUID userId, String newStatus) {
        super("USER", userId);
        this.newStatus = newStatus;
    }
}