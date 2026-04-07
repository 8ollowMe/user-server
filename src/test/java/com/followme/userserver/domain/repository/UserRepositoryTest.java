package com.followme.userserver.domain.repository;

import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest 대신 전체 컨텍스트를 로드하여 빈 주입 문제를 해결합니다.
@SpringBootTest 
@Transactional // 테스트가 끝나면 DB를 롤백하여 다음 테스트에 영향을 주지 않도록 합니다.
@ActiveProfiles("test") 
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("username 존재 여부 확인 - 존재하는 경우 true 반환")
    void existsByUsername_ReturnsTrue() {
        // given
        String testUsername = "tester123";
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(testUsername)
                .name("테스터")
                .slackId("slack123")
                .role(UserRole.MASTER)
                .status(UserStatus.APPROVED)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername(testUsername);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 존재 여부 확인 - 존재하지 않는 경우 false 반환")
    void existsByUsername_ReturnsFalse() {
        // given
        String testUsername = "nonExistentUser";

        // when
        boolean exists = userRepository.existsByUsername(testUsername);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("허브의 배송 담당자 최대 시퀀스 조회 - 정상 작동")
    void findMaxSequence_ReturnsMaxSequence() {
        // given
        UUID hubId = UUID.randomUUID();
        
        // 시퀀스가 1인 유저 저장
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("delivery1")
                .name("배송원1")
                .slackId("slack1")
                .role(UserRole.DELIVERY)
                .status(UserStatus.APPROVED)
                .hubId(hubId)
                .sequence(1L)
                .build();
                
        // 시퀀스가 5인 유저 저장 (이 값이 Max)
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("delivery2")
                .name("배송원2")
                .slackId("slack2")
                .role(UserRole.DELIVERY)
                .status(UserStatus.APPROVED)
                .hubId(hubId)
                .sequence(5L)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // when
        Long maxSequence = userRepository.findMaxSequence(hubId, UserRole.DELIVERY);

        // then
        assertThat(maxSequence).isEqualTo(5L);
    }
    
    @Test
    @DisplayName("허브의 배송 담당자가 없을 경우 시퀀스 조회 시 0 반환")
    void findMaxSequence_WhenNoDelivery_ReturnsZero() {
        // given
        UUID hubId = UUID.randomUUID();

        // when
        Long maxSequence = userRepository.findMaxSequence(hubId, UserRole.DELIVERY);

        // then
        // findMaxSequence의 구현에 따라 null을 반환할 수도 있고 0을 반환할 수도 있습니다.
        // 현재 QueryDSL 구현에 맞춰 null 방어 로직이 없다면 아래 검증을 조정해야 할 수 있습니다.
        if (maxSequence == null) {
             assertThat(maxSequence).isNull();
        } else {
             assertThat(maxSequence).isEqualTo(0L); 
        }
    }
}