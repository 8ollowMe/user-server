package com.followme.userserver.domain.event;

import com.followMe.common.event.BaseEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class UserCreatedEvent extends BaseEvent {
    private final String username;
    private final String role;

    public UserCreatedEvent(UUID userId, String username, String role) {
        super("USER", userId);
        this.username = username;
        this.role = role;
    }
}