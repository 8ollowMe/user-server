package com.followme.userserver.domain.event;

import com.followMe.common.event.BaseEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class UserDeactivatedEvent extends BaseEvent {
    public UserDeactivatedEvent(UUID userId) {
        super("USER", userId);
    }
}