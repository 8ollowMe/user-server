package com.followme.userserver.application.service;

import com.followme.userserver.application.dto.UserRegisterRequestDto;
import com.followme.userserver.application.dto.UserResponseDto;
import com.followme.userserver.application.dto.UserUpdateRequestDto;
import com.followme.userserver.application.mapper.UserMapper;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.repository.UserRepository;
// 💡 새롭게 만든 예외 클래스들을 import 합니다.
import com.followme.userserver.exception.DuplicateUsernameException;
import com.followme.userserver.exception.KeycloakSyncException;
import com.followme.userserver.exception.UserNotFoundException;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Keycloak keycloak;
    private final UserMapper userMapper;

    @Value("${keycloak.realm}")
    private String realm;
    
    @Transactional
    public void registerUser(UserRegisterRequestDto request) {
        
        // 1. 아이디 중복 검증 
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }


        // 2-1. Keycloak에 보낼 유저 정보
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(request.getUsername());
        kcUser.setEnabled(true);
        kcUser.setLastName(request.getName());
        kcUser.setFirstName("."); // Keycloak은 firstName이 필수라서 임의로 넣어줍니다.
        kcUser.setEmail(request.getUsername() + "@test.com"); // Keycloak은 이메일이 필수라서 임의로 넣어줍니다.

        // 2-2. Keycloak에 보낼 비밀번호
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        
        // 2-3. 유저 정보 안에 비밀번호 넣기
        kcUser.setCredentials(List.of(credential));

        // 2-4. 핫라인으로 API 전송
        Response response = keycloak.realm(realm).users().create(kcUser);

        if (response.getStatus() != 201) {
            throw new KeycloakSyncException();
        }
        // 3. Keycloak이 방금 생성한 유저의 UUID 추출하기
        String path = response.getLocation().getPath();
        String keycloakUserId = path.substring(path.lastIndexOf('/') + 1);

        // 4. 로컬 DB용 엔티티
        User newUser = userMapper.toEntity(request);

        // 5. 뽑아낸 Keycloak ID를 로컬 엔티티의 ID로 강제 주입
        newUser.setId(UUID.fromString(keycloakUserId));

        // 6. DB에 저장
        userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserProfile(String username) {
        // 1. DB에서 유저 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        // 2. MapStruct를 이용해 Entity -> DTO 변환 후 반환
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(String username, UserUpdateRequestDto request) {
        
        // 1. 유저 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        // 2. 엔티티 비즈니스 메서드 호출 (데이터 수정)
        user.updateProfile(request);

        // 3. 수정된 결과를 다시 DTO로 변환하여 반환
        return userMapper.toResponseDto(user);
    }

    @Transactional
    public void deactivateMyAccount(String username) {
        // 1. 유저 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        // 2. 토큰 인증 객체에서 직접 ID(Keycloak UUID)를 추출
        String currentTokenUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        user.deactivateAccount(currentTokenUserId);
        
        try {
            // 3-1. Keycloak에서 해당 username으로 유저 검색
            List<UserRepresentation> kcUsers = keycloak.realm(realm).users().search(username);
            
            if (kcUsers != null && !kcUsers.isEmpty()) {
                // 검색된 유저(보통 1명) 정보 가져오기
                UserRepresentation kcUser = kcUsers.get(0); 
                
                // 3-2. 상태를 비활성화(Enabled = false)로 변경
                kcUser.setEnabled(false); 
                
                // 3-3. Keycloak 서버에 변경된 정보 업데이트 요청
                keycloak.realm(realm).users().get(kcUser.getId()).update(kcUser);
            } else {
                // Keycloak에 유저가 없는 극히 드문 예외 상황 처리
                throw new KeycloakSyncException(); 
            }
        } catch (Exception e) {
            // Keycloak 서버 통신 장애 등 예외 발생 시 트랜잭션 롤백을 위해 예외 던지기
            throw new KeycloakSyncException(); 
        }
    }
}