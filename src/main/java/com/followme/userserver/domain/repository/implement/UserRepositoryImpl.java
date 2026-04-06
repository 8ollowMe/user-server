package com.followme.userserver.domain.repository.implement;

import com.followme.userserver.application.dto.UserRequest;
import com.followme.userserver.domain.entity.User;
import com.followme.userserver.domain.enums.UserRole;
import com.followme.userserver.domain.enums.UserStatus;
import com.followme.userserver.domain.repository.UserRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static com.followme.userserver.domain.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<User> searchUsers(UserRequest.SearchCondition condition, Pageable pageable) {
        List<User> users = queryFactory
                .selectFrom(user)
                .where(
                        roleEq(condition.getRole()),
                        statusEq(condition.getStatus()),
                        keywordContains(condition.getKeyword()),
                        user.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(user.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(
                        roleEq(condition.getRole()),
                        statusEq(condition.getStatus()),
                        keywordContains(condition.getKeyword()),
                        user.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(users, pageable, total == null ? 0 : total);
    }

    @Override
    public List<User> findManagersByHubId(UUID hubId) {
        return queryFactory
                .selectFrom(user)
                .where(
                        hubIdEq(hubId),
                        user.role.in(UserRole.HUB, UserRole.MASTER),
                        user.deletedAt.isNull()
                )
                .fetch();
    }

    @Override
    public List<User> findDeliveries(UUID hubId, UUID vendorId) {
        return queryFactory
                .selectFrom(user)
                .where(
                        user.role.eq(UserRole.DELIVERY),
                        hubIdEq(hubId),
                        vendorIdEq(vendorId),
                        user.deletedAt.isNull()
                )
                .orderBy(user.sequence.asc().nullsLast())
                .fetch();
    }

    @Override
    public Long findMaxSequence(UUID hubId, UserRole role) {
        Long maxSeq = queryFactory
                .select(user.sequence.max())
                .from(user)
                .where(
                        hubIdEq(hubId), // Null일 경우 isNull 조건으로 동작
                        roleEq(role),
                        user.deletedAt.isNull()
                )
                .fetchOne();

        return maxSeq == null ? 0L : maxSeq;
    }

    // --- 동적 쿼리 조건 생성 메서드 ---

    private BooleanExpression roleEq(UserRole role) {
        return role != null ? user.role.eq(role) : null;
    }

    private BooleanExpression statusEq(UserStatus status) {
        return status != null ? user.status.eq(status) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        return (keyword != null && !keyword.isBlank())
                ? user.name.containsIgnoreCase(keyword).or(user.username.containsIgnoreCase(keyword))
                : null;
    }

    private BooleanExpression hubIdEq(UUID hubId) {
        return hubId != null ? user.hubId.eq(hubId) : user.hubId.isNull();
    }

    private BooleanExpression vendorIdEq(UUID vendorId) {
        return vendorId != null ? user.vendorId.eq(vendorId) : null;
    }
}