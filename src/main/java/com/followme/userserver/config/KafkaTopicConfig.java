package com.followme.userserver.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 1. 회원가입 이벤트 토픽
    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name("UserCreatedEvent")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // 2. 유저 상태 변경 이벤트 토픽
    @Bean
    public NewTopic userStatusUpdatedTopic() {
        return TopicBuilder.name("UserStatusUpdatedEvent")
                .partitions(3)
                .replicas(3)
                .build();
    }

    // 3. 계정 비활성화 이벤트 토픽
    @Bean
    public NewTopic userDeactivatedTopic() {
        return TopicBuilder.name("UserDeactivatedEvent")
                .partitions(3)
                .replicas(3)
                .build();
    }

    // 4. 배송 담당자 순번 업데이트 이벤트 토픽
    @Bean
    public NewTopic deliverySequenceUpdatedTopic() {
        return TopicBuilder.name("DeliverySequenceUpdatedEvent")
                .partitions(3)
                .replicas(3)
                .build();
    }
}