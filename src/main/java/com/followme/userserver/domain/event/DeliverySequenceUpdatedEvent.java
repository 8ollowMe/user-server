package com.followme.userserver.domain.event;

import com.followMe.common.event.BaseEvent;
import lombok.Getter;
import java.util.UUID;

@Getter
public class DeliverySequenceUpdatedEvent extends BaseEvent {
    private final Long newSequence;

    public DeliverySequenceUpdatedEvent(UUID userId, Long newSequence) {
        super("USER", userId);
        this.newSequence = newSequence;
    }
}