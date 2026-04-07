package com.followme.userserver.application.service;

import com.followMe.common.event.Events;
import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.application.dto.UserResponse;
import com.followme.userserver.application.mapper.UserMapper;
import com.followme.userserver.application.port.AuthPort;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import com.followme.userserver.domain.repository.UserRepository;
import com.followme.userserver.exception.DuplicateUsernameException;
import com.followme.userserver.exception.InvalidDeliveryAssociationException;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @InjectMocks
    private UserCommandService userCommandService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private Keycloak keycloak;
    @Mock
    private AuthPort authPort;

    // Keycloak 체이닝(realm().users()) 모킹을 위한 객체들
    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;

    private MockedStatic<Events> mockedEvents;

    private final String realm = "test-realm";
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // @Value("${keycloak.realm}") 값 강제 주입
        ReflectionTestUtils.setField(userCommandService, "realm", realm);
        
        // 정적 메서드인 Events.trigger() 모킹
        mockedEvents = mockStatic(Events.class);
    }

    @AfterEach
    void tearDown() {
        // 정적 모킹 해제 (다른 테스트에 영향 주지 않도록)
        mockedEvents.close();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원가입 성공 - 정상적인 요청일 때 Keycloak과 DB에 모두 저장된다")
    void registerUser_Success() {
        // given
        UserRequest.Register request = new UserRequest.Register();
        ReflectionTestUtils.setField(request, "username", "user01");
        ReflectionTestUtils.setField(request, "password", "Password123!");
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "hubId", UUID.randomUUID());
        ReflectionTestUtils.setField(request, "role", UserRole.HUB);
        

        User mockUser = User.builder().id(testUserId).username("testuser").role(UserRole.HUB).build();

        given(userRepository.existsByUsername(request.getUsername())).willReturn(false);
        
        // Keycloak 모킹 (201 Created 응답 및 Location 헤더 세팅)
        Response mockResponse = Response.status(201).location(URI.create("http://auth/users/" + testUserId)).build();
        given(keycloak.realm(realm)).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);
        given(usersResource.create(any())).willReturn(mockResponse);

        given(userMapper.toEntity(request)).willReturn(mockUser);

        // when
        userCommandService.registerUser(request);

        // then
        verify(userRepository, times(1)).save(any(User.class));
        mockedEvents.verify(() -> Events.trigger(any()), times(1)); // 이벤트 트리거 확인
    }

    @Test
    @DisplayName("회원가입 실패 - 중복된 username이 있으면 예외가 발생한다")
    void registerUser_DuplicateUsername_ThrowsException() {
        // given
        UserRequest.Register request = new UserRequest.Register();
        ReflectionTestUtils.setField(request, "username", "user02");

        given(userRepository.existsByUsername(request.getUsername())).willReturn(true);

        // when & then
        assertThrows(DuplicateUsernameException.class, () -> userCommandService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 배송 담당자인데 업체(vendor) 아이디가 있으면 예외가 발생한다")
    void registerUser_DeliveryRoleWithVendorId_ThrowsException() {
        // given
        UserRequest.Register request = new UserRequest.Register();
        ReflectionTestUtils.setField(request, "username", "user01");
        ReflectionTestUtils.setField(request, "role", UserRole.DELIVERY);
        ReflectionTestUtils.setField(request, "vendorId", UUID.randomUUID());

        given(userRepository.existsByUsername(request.getUsername())).willReturn(false);

        // when & then
        assertThrows(InvalidDeliveryAssociationException.class, () -> userCommandService.registerUser(request));
    }

    @Test
    @DisplayName("상태 업데이트 - APPROVE 요청 시 승인 처리되고 Keycloak 및 AuthPort가 동기화된다")
    void updateUserStatus_Approve_Success() {
        // given
        UserRequest.UpdateStatus request = new UserRequest.UpdateStatus();
        ReflectionTestUtils.setField(request, "status", UserStatus.APPROVED);

        User mockUser = User.builder().id(testUserId).role(UserRole.HUB).status(UserStatus.PENDING).build();
        UserResponse.Info mockResponse = mock(UserResponse.Info.class);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(mockUser));
        given(userMapper.toResponseDto(mockUser)).willReturn(mockResponse);

        // when
        userCommandService.updateUserStatus(testUserId, request);

        // then
        assertThat(mockUser.getStatus()).isEqualTo(UserStatus.APPROVED);
        verify(authPort, times(1)).syncKeycloakUserStatus(testUserId, true);
        verify(authPort, times(1)).assignRealmRole(testUserId, UserRole.HUB.name());
        mockedEvents.verify(() -> Events.trigger(any()), times(1));
    }

    @Test
    @DisplayName("계정 비활성화 - SecurityContextHolder에서 현재 유저를 꺼내와 Soft Delete 처리한다")
    void deactivateAccount_Success() {
        // given
        User mockUser = User.builder().id(testUserId).status(UserStatus.APPROVED).build();
        String currentUserIdStr = UUID.randomUUID().toString();

        // SecurityContext 모킹 (현재 로그인한 유저 세팅)
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getName()).willReturn(currentUserIdStr);
        SecurityContextHolder.setContext(securityContext);

        given(userRepository.findById(testUserId)).willReturn(Optional.of(mockUser));
        
        // Keycloak 모킹 (비활성화 처리용)
        org.keycloak.admin.client.resource.UserResource userResourceMock = mock(org.keycloak.admin.client.resource.UserResource.class);
        org.keycloak.representations.idm.UserRepresentation userRepMock = new org.keycloak.representations.idm.UserRepresentation();
        
        given(keycloak.realm(realm)).willReturn(realmResource);
        given(realmResource.users()).willReturn(usersResource);
        given(usersResource.get(testUserId.toString())).willReturn(userResourceMock);
        given(userResourceMock.toRepresentation()).willReturn(userRepMock);

        // when
        userCommandService.deactivateAccount(testUserId);

        // then
        assertThat(mockUser.getStatus()).isEqualTo(UserStatus.DELETED);
        verify(userResourceMock, times(1)).update(any()); // Keycloak 업데이트 호출 확인
        mockedEvents.verify(() -> Events.trigger(any()), times(1));
    }
}